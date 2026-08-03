package com.bank.fraud.service.impl;

import com.bank.fraud.dto.event.FraudResultEventDTO;
import com.bank.fraud.dto.event.TransactionEventDTO;
import com.bank.fraud.engine.RuleEngine;
import com.bank.fraud.service.FraudDetectionService;
import com.bank.fraud.repository.FraudAlertRepository;
import com.bank.fraud.entity.FraudAlert;
import com.bank.fraud.entity.AlertStatus;
import com.bank.fraud.entity.AlertSeverity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final RuleEngine ruleEngine;
    private final KafkaTemplate<String, FraudResultEventDTO> kafkaTemplate;
    private final FraudAlertRepository fraudAlertRepository;

    @Override
    public void processTransaction(TransactionEventDTO event) {
        log.info("Evaluating fraud for transaction ID: {}", event.getTransactionId());
        
        FraudResultEventDTO result = ruleEngine.evaluate(event);
        
        if (!result.getApproved()) {
            FraudAlert alert = FraudAlert.builder()
                    .transactionId(event.getTransactionId())
                    .senderAccountId(event.getSenderAccountId())
                    .receiverAccountId(event.getReceiverAccountId())
                    .amount(event.getAmount())
                    .reason(result.getReason())
                    .riskScore(extractScore(result.getReason()))
                    .status(AlertStatus.OPEN)
                    .build();
            fraudAlertRepository.save(alert);
            log.info("Fraud alert saved for TX: {}", event.getTransactionId());
        }
        
        String topic = result.getApproved() ? "transaction-approved" : "transaction-rejected";
        kafkaTemplate.send(topic, result.getTransactionId(), result);
        
        log.info("Fraud evaluation completed for TX {}: {}, reason: {}. Published to topic {}", 
                result.getTransactionId(), result.getApproved(), result.getReason(), topic);
    }

    private int extractScore(String reason) {
        try {
            // Reason format: "REJECTED: Total Risk Score 150 exceeded..."
            String[] parts = reason.split("Risk Score ");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1].split(" ")[0]);
            }
        } catch (Exception e) {
            log.warn("Could not parse risk score from reason", e);
        }
        return 0;
    }
}
