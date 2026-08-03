package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ThresholdRule implements FraudRule {

    @Value("${fraud.rules.threshold.high-amount:10000}")
    private BigDecimal highAmount;

    @Value("${fraud.rules.threshold.max-amount:100000}")
    private BigDecimal maxAmount;

    @Override
    public int calculateRiskScore(TransactionEventDTO event) {
        if (event.getAmount().compareTo(maxAmount) > 0) {
            return 100; // Impossible amount, instant block
        }
        if (event.getAmount().compareTo(highAmount) > 0) {
            return 30; // 30 points for unusually high amount
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "ThresholdRule";
    }
}
