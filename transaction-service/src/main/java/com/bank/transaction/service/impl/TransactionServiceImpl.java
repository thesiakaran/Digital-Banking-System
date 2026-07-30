package com.bank.transaction.service.impl;

import com.bank.transaction.dto.event.TransactionEventDTO;
import com.bank.transaction.dto.request.InitiateTransactionDTO;
import com.bank.transaction.dto.response.TransactionStatusResponseDTO;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.entity.TransactionType;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.bank.transaction.dto.event.FraudResultEventDTO;
import com.bank.transaction.dto.event.DepositEventDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, TransactionEventDTO> kafkaTemplate;
    private final KafkaTemplate<String, DepositEventDTO> depositKafkaTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${account-service.url}")
    private String accountServiceUrl;

    @Override
    @Transactional
    public TransactionStatusResponseDTO initiateTransaction(InitiateTransactionDTO request, String idempotencyKey, String correlationId) {
        // Strict Idempotency Check
        Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTx.isPresent()) {
            log.info("Idempotent request received for key: {}", idempotencyKey);
            return mapToResponse(existingTx.get());
        }

        Transaction transaction = Transaction.builder()
                .idempotencyKey(idempotencyKey)
                .senderAccountNumber(request.getSenderAccountNumber())
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .type(request.getType() != null ? request.getType() : TransactionType.TRANSFER)
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction initialized with ID: {}", transaction.getTransactionId());

        boolean isDeposit = transaction.getType() == TransactionType.DEPOSIT;

        try {
            if (!isDeposit) {
                // Call Account Service to Hold Funds
                Map<String, Object> holdRequest = new HashMap<>();
                holdRequest.put("accountNumber", request.getSenderAccountNumber());
                holdRequest.put("amount", request.getAmount());
                holdRequest.put("transactionId", transaction.getTransactionId());

                WebClient webClient = webClientBuilder.baseUrl(accountServiceUrl).build();
                webClient.post()
                        .uri("/api/accounts/hold")
                        .bodyValue(holdRequest)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block(); // Synchronous hold
            }

            transaction.setStatus(TransactionStatus.FUNDS_HELD);
            transactionRepository.save(transaction);
            
            // Publish Event for Fraud Evaluation
            TransactionEventDTO event = TransactionEventDTO.builder()
                    .transactionId(transaction.getTransactionId())
                    .senderAccountId(transaction.getSenderAccountNumber())
                    .receiverAccountId(transaction.getReceiverAccountNumber())
                    .amount(transaction.getAmount())
                    .timestamp(Instant.now().toEpochMilli())
                    .correlationId(correlationId)
                    .build();

            kafkaTemplate.send("transaction-created", transaction.getTransactionId(), event);
            log.info("Published transaction-created event for TX: {}", transaction.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to hold funds or publish event for TX: {}", transaction.getTransactionId(), e);
            transaction.setStatus(TransactionStatus.REJECTED);
            transaction.setFailureReason("Failed to hold funds: " + e.getMessage());
            transactionRepository.save(transaction);
        }

        return mapToResponse(transaction);
    }

    @Override
    public TransactionStatusResponseDTO getTransactionStatus(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return mapToResponse(transaction);
    }

    @Override
    @Transactional
    public void processFraudResult(FraudResultEventDTO result) {
        Transaction transaction = transactionRepository.findByTransactionId(result.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found for saga completion: " + result.getTransactionId()));

        if (transaction.getStatus() != TransactionStatus.FUNDS_HELD) {
            log.warn("Transaction {} is not in FUNDS_HELD state. Current state: {}", transaction.getTransactionId(), transaction.getStatus());
            return; // Idempotency guard for saga
        }

        WebClient webClient = webClientBuilder.baseUrl(accountServiceUrl).build();

        try {
            boolean isDeposit = transaction.getType() == TransactionType.DEPOSIT;
            boolean isWithdrawal = transaction.getType() == TransactionType.WITHDRAWAL;

            if (Boolean.TRUE.equals(result.getApproved())) {
                if (!isDeposit) {
                    webClient.post()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/api/accounts/{accountNumber}/commit")
                                    .queryParam("amount", transaction.getAmount())
                                    .queryParam("transactionId", transaction.getTransactionId())
                                    .build(transaction.getSenderAccountNumber()))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();
                }

                if (!isWithdrawal) {
                    // Credit the Receiver's account!
                    webClient.post()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/api/accounts/{accountNumber}/deposit")
                                    .queryParam("amount", transaction.getAmount())
                                    .build(transaction.getReceiverAccountNumber()))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();
                }

                transaction.setStatus(TransactionStatus.COMPLETED);
                log.info("SAGA Completed successfully for TX: {}", transaction.getTransactionId());
            } else {
                if (!isDeposit) {
                    webClient.post()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/api/accounts/{accountNumber}/release")
                                    .queryParam("amount", transaction.getAmount())
                                    .queryParam("transactionId", transaction.getTransactionId())
                                    .build(transaction.getSenderAccountNumber()))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();
                }

                transaction.setStatus(TransactionStatus.ROLLED_BACK);
                transaction.setFailureReason(result.getReason());
                log.info("SAGA Rolled Back for TX: {}. Reason: {}", transaction.getTransactionId(), result.getReason());
            }
        } catch (Exception e) {
            log.error("Failed to complete SAGA for TX: {}", transaction.getTransactionId(), e);
        }

        transactionRepository.save(transaction);
    }

    private TransactionStatusResponseDTO mapToResponse(Transaction transaction) {
        String simplifiedStatus = "PENDING";
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            simplifiedStatus = "SUCCESS";
        } else if (transaction.getStatus() == TransactionStatus.REJECTED || transaction.getStatus() == TransactionStatus.ROLLED_BACK) {
            simplifiedStatus = "FAILURE";
        }
        
        String dateTimeStr = transaction.getCreatedAt() != null 
                ? transaction.getCreatedAt().toString() 
                : java.time.LocalDateTime.now().toString();

        return TransactionStatusResponseDTO.builder()
                .amount(transaction.getAmount())
                .status(simplifiedStatus)
                .dateAndTime(dateTimeStr)
                .build();
    }

    @Override
    public java.util.List<com.bank.transaction.dto.response.TransactionHistoryResponseDTO> getAccountHistory(String accountNumber) {
        return transactionRepository.findBySenderAccountNumberOrReceiverAccountNumberOrderByCreatedAtDesc(accountNumber, accountNumber)
                .stream()
                .map(tx -> mapToHistoryResponse(tx, accountNumber))
                .toList();
    }

    private com.bank.transaction.dto.response.TransactionHistoryResponseDTO mapToHistoryResponse(Transaction transaction, String accountNumber) {
        String simplifiedStatus = "PENDING";
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            simplifiedStatus = "SUCCESS";
        } else if (transaction.getStatus() == TransactionStatus.REJECTED || transaction.getStatus() == TransactionStatus.ROLLED_BACK) {
            simplifiedStatus = "FAILURE";
        }

        String type = transaction.getType() != null ? transaction.getType().name() : "TRANSFER";
        
        String relatedAccount = "";
        if (accountNumber.equals(transaction.getSenderAccountNumber())) {
            relatedAccount = transaction.getReceiverAccountNumber();
            if (transaction.getType() == TransactionType.WITHDRAWAL) {
                relatedAccount = "CASH OUT";
            }
        } else {
            relatedAccount = transaction.getSenderAccountNumber();
            if (transaction.getType() == TransactionType.DEPOSIT) {
                relatedAccount = "CASH IN";
            }
        }

        return com.bank.transaction.dto.response.TransactionHistoryResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .type(type)
                .amount(transaction.getAmount())
                .status(simplifiedStatus)
                .dateAndTime(transaction.getCreatedAt() != null ? transaction.getCreatedAt().toString() : "")
                .relatedAccount(relatedAccount)
                .build();
    }
}
