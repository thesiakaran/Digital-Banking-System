package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RoundAmountRule implements FraudRule {

    @Override
    public int calculateRiskScore(TransactionEventDTO event) {
        BigDecimal amount = event.getAmount();
        
        // If amount > $1000 and ends in .00 or is a perfect multiple of 1000
        if (amount.compareTo(new BigDecimal("1000")) >= 0) {
            if (amount.remainder(new BigDecimal("1000")).compareTo(BigDecimal.ZERO) == 0) {
                return 15; // 15 points for massive perfectly round number
            }
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "RoundAmountRule";
    }
}
