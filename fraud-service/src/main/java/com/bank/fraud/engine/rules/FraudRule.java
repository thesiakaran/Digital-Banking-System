package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;

/**
 * Interface for all fraud detection rules in the Weighted Risk Scoring Engine.
 */
public interface FraudRule {
    
    /**
     * Calculates the risk score for this specific rule.
     * @param event The transaction event
     * @return Risk score (0 if no risk, higher value for higher risk)
     */
    int calculateRiskScore(TransactionEventDTO event);
    
    /**
     * @return The name of the rule for logging purposes
     */
    String getRuleName();
}
