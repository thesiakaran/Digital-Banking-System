package com.bank.fraud.listener;

import com.bank.fraud.dto.event.TransactionEventDTO;
import com.bank.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(topics = "transaction-created", groupId = "fraud-evaluation-group")
    public void consumeTransactionEvent(TransactionEventDTO event) {
        log.info("Received transaction-created event for TX: {}", event.getTransactionId());
        fraudDetectionService.processTransaction(event);
    }
}
