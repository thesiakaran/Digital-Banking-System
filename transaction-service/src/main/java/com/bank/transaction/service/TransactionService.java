package com.bank.transaction.service;

import com.bank.transaction.dto.request.InitiateTransactionDTO;
import com.bank.transaction.dto.response.TransactionStatusResponseDTO;

public interface TransactionService {
    TransactionStatusResponseDTO initiateTransaction(InitiateTransactionDTO request, String idempotencyKey, String correlationId);
    TransactionStatusResponseDTO getTransactionStatus(String transactionId);
    void processFraudResult(com.bank.transaction.dto.event.FraudResultEventDTO result);
    java.util.List<com.bank.transaction.dto.response.TransactionHistoryResponseDTO> getAccountHistory(String accountNumber);
}
