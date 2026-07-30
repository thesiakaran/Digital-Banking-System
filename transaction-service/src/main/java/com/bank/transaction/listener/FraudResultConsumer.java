package com.bank.transaction.listener;

import com.bank.transaction.dto.event.FraudResultEventDTO;
import com.bank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudResultConsumer {

    private final TransactionService transactionService;

    @KafkaListener(topics = {"transaction-approved", "transaction-rejected"}, groupId = "transaction-saga-group")
    public void consumeFraudResult(FraudResultEventDTO result) {
        log.info("Received fraud evaluation result for TX {}: approved={}", result.getTransactionId(), result.getApproved());
        transactionService.processFraudResult(result);
    }
}
