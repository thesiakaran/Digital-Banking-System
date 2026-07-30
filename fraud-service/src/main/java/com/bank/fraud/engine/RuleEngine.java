package com.bank.fraud.engine;

import com.bank.fraud.dto.event.FraudResultEventDTO;
import com.bank.fraud.dto.event.TransactionEventDTO;
import com.bank.fraud.engine.rules.BlacklistRule;
import com.bank.fraud.engine.rules.ThresholdRule;
import com.bank.fraud.engine.rules.VelocityRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RuleEngine {

    private final VelocityRule velocityRule;
    private final BlacklistRule blacklistRule;
    private final ThresholdRule thresholdRule;
    private final com.bank.fraud.engine.rules.DailyLimitRule dailyLimitRule;

    public FraudResultEventDTO evaluate(TransactionEventDTO event) {
        if (blacklistRule.isFraud(event)) {
            return buildResult(event.getTransactionId(), false, "Account is blacklisted");
        }
        if (thresholdRule.isFraud(event)) {
            return buildResult(event.getTransactionId(), false, "High value transaction threshold breached");
        }
        if (velocityRule.isFraud(event)) {
            return buildResult(event.getTransactionId(), false, "Velocity limit breached");
        }
        if (dailyLimitRule.isFraud(event)) {
            return buildResult(event.getTransactionId(), false, "Daily transaction limit breached");
        }
        
        return buildResult(event.getTransactionId(), true, "Approved");
    }

    private FraudResultEventDTO buildResult(String transactionId, boolean approved, String reason) {
        return FraudResultEventDTO.builder()
                .transactionId(transactionId)
                .approved(approved)
                .reason(reason)
                .evaluatedAt(Instant.now().toEpochMilli())
                .build();
    }
}
