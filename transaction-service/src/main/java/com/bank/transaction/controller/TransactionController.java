package com.bank.transaction.controller;

import com.bank.transaction.dto.request.InitiateTransactionDTO;
import com.bank.transaction.dto.response.TransactionStatusResponseDTO;
import com.bank.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionStatusResponseDTO> initiateTransaction(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @Valid @RequestBody InitiateTransactionDTO request) {
        return new ResponseEntity<>(transactionService.initiateTransaction(request, idempotencyKey, correlationId), HttpStatus.CREATED);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionStatusResponseDTO> getTransactionStatus(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionStatus(transactionId));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<java.util.List<com.bank.transaction.dto.response.TransactionHistoryResponseDTO>> getAccountHistory(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getAccountHistory(accountNumber));
    }
}
