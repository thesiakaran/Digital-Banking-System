package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ThresholdRule {

    @Value("${fraud.rules.threshold.max-amount:100000}")
    private BigDecimal maxAmount;

    public boolean isFraud(TransactionEventDTO event) {
        return event.getAmount().compareTo(maxAmount) > 0;
    }
}
