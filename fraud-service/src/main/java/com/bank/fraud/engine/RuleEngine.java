package com.bank.fraud.engine;

import com.bank.fraud.dto.event.FraudResultEventDTO;
import com.bank.fraud.dto.event.TransactionEventDTO;
import com.bank.fraud.engine.rules.FraudRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {

    // Spring will automatically inject all beans that implement the FraudRule interface
    private final List<FraudRule> rules;

    @Value("${fraud.rules.max-risk-score:100}")
    private int maxRiskScore;

    public FraudResultEventDTO evaluate(TransactionEventDTO event) {
        int totalRiskScore = 0;
        StringBuilder reasons = new StringBuilder("Risk reasons: ");

        log.info("Starting Weighted Risk Evaluation for TX: {}", event.getTransactionId());

        for (FraudRule rule : rules) {
            int score = rule.calculateRiskScore(event);
            if (score > 0) {
                totalRiskScore += score;
                reasons.append(rule.getRuleName()).append("(+").append(score).append(") ");
                log.info("Rule {} triggered. Added {} points. Total Score: {}", rule.getRuleName(), score, totalRiskScore);
            }
        }

        boolean approved = totalRiskScore < maxRiskScore;
        String finalReason = approved ? "Approved with Risk Score: " + totalRiskScore : 
                                        "REJECTED: Total Risk Score " + totalRiskScore + " exceeded maximum threshold of " + maxRiskScore + ". " + reasons.toString();

        return buildResult(event.getTransactionId(), approved, finalReason);
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
