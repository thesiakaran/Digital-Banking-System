package com.bank.fraud.service;

import com.bank.fraud.dto.event.TransactionEventDTO;

public interface FraudDetectionService {
    void processTransaction(TransactionEventDTO event);
}
