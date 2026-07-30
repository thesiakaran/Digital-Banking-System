package com.bank.fraud.service.impl;

import com.bank.fraud.dto.event.FraudResultEventDTO;
import com.bank.fraud.dto.event.TransactionEventDTO;
import com.bank.fraud.engine.RuleEngine;
import com.bank.fraud.service.FraudDetectionService;
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

    @Override
    public void processTransaction(TransactionEventDTO event) {
        log.info("Evaluating fraud for transaction ID: {}", event.getTransactionId());
        
        FraudResultEventDTO result = ruleEngine.evaluate(event);
        
        String topic = result.getApproved() ? "transaction-approved" : "transaction-rejected";
        kafkaTemplate.send(topic, result.getTransactionId(), result);
        
        log.info("Fraud evaluation completed for TX {}: {}, reason: {}. Published to topic {}", 
                result.getTransactionId(), result.getApproved(), result.getReason(), topic);
    }
}
