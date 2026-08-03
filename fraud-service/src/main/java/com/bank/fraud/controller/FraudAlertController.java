package com.bank.fraud.controller;

import com.bank.fraud.dto.response.FraudAlertResponseDTO;
import com.bank.fraud.dto.response.FraudStatisticsDTO;
import com.bank.fraud.entity.AlertStatus;
import com.bank.fraud.entity.FraudAlert;
import com.bank.fraud.repository.FraudAlertRepository;
import com.bank.fraud.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudAlertController {

    private final FraudAlertRepository fraudAlertRepository;
    private final BlacklistService blacklistService;

    @GetMapping("/alerts")
    public ResponseEntity<Page<FraudAlertResponseDTO>> getAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FraudAlert> alerts;
        if (status != null && !status.isEmpty()) {
            alerts = fraudAlertRepository.findByStatusOrderByDetectedAtDesc(
                    AlertStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            alerts = fraudAlertRepository.findAllByOrderByDetectedAtDesc(pageable);
        }
        return ResponseEntity.ok(alerts.map(this::toDTO));
    }

    @GetMapping("/alerts/{id}")
    public ResponseEntity<FraudAlertResponseDTO> getAlert(@PathVariable Long id) {
        return fraudAlertRepository.findById(id)
                .map(alert -> ResponseEntity.ok(toDTO(alert)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/alerts/{id}/status")
    public ResponseEntity<FraudAlertResponseDTO> updateAlertStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return fraudAlertRepository.findById(id)
                .map(alert -> {
                    alert.setStatus(AlertStatus.valueOf(status.toUpperCase()));
                    if (alert.getStatus() == AlertStatus.RESOLVED || alert.getStatus() == AlertStatus.FALSE_POSITIVE) {
                        alert.setResolvedAt(LocalDateTime.now());
                    }
                    fraudAlertRepository.save(alert);
                    return ResponseEntity.ok(toDTO(alert));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/statistics")
    public ResponseEntity<FraudStatisticsDTO> getStatistics() {
        List<Object[]> topAccounts = fraudAlertRepository.findTopFlaggedAccounts(PageRequest.of(0, 5));
        List<Map<String, Object>> topFlagged = new ArrayList<>();
        for (Object[] row : topAccounts) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("accountId", row[0]);
            entry.put("alertCount", row[1]);
            topFlagged.add(entry);
        }

        FraudStatisticsDTO stats = FraudStatisticsDTO.builder()
                .totalAlerts(fraudAlertRepository.count())
                .openAlerts(fraudAlertRepository.countByStatus(AlertStatus.OPEN))
                .investigatingAlerts(fraudAlertRepository.countByStatus(AlertStatus.INVESTIGATING))
                .resolvedAlerts(fraudAlertRepository.countByStatus(AlertStatus.RESOLVED))
                .topFlaggedAccounts(topFlagged)
                .build();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/blacklist/{accountId}")
    public ResponseEntity<Map<String, String>> addToBlacklist(@PathVariable String accountId) {
        blacklistService.addToBlacklist(accountId);
        return ResponseEntity.ok(Map.of("message", "Account " + accountId + " added to blacklist", "accountId", accountId));
    }

    @DeleteMapping("/blacklist/{accountId}")
    public ResponseEntity<Map<String, String>> removeFromBlacklist(@PathVariable String accountId) {
        blacklistService.removeFromBlacklist(accountId);
        return ResponseEntity.ok(Map.of("message", "Account " + accountId + " removed from blacklist", "accountId", accountId));
    }

    @GetMapping("/blacklist")
    public ResponseEntity<Set<String>> getBlacklist() {
        return ResponseEntity.ok(blacklistService.getBlacklist());
    }

    private FraudAlertResponseDTO toDTO(FraudAlert alert) {
        return FraudAlertResponseDTO.builder()
                .id(alert.getId())
                .transactionId(alert.getTransactionId())
                .senderAccountId(alert.getSenderAccountId())
                .receiverAccountId(alert.getReceiverAccountId())
                .amount(alert.getAmount())
                .reason(alert.getReason())
                .riskScore(alert.getRiskScore())
                .status(alert.getStatus().name())
                .severity(alert.getSeverity().name())
                .detectedAt(alert.getDetectedAt() != null ? alert.getDetectedAt().toString() : null)
                .resolvedAt(alert.getResolvedAt() != null ? alert.getResolvedAt().toString() : null)
                .build();
    }
}
