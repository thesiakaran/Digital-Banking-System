package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RoundAmountRuleTest {

    private RoundAmountRule rule;

    @BeforeEach
    void setUp() {
        rule = new RoundAmountRule();
    }

    @Test
    @DisplayName("Should return 15 for round amount of exactly 1000")
    void roundAmount1000_returns15() {
        assertEquals(15, rule.calculateRiskScore(buildEvent("1000")));
    }

    @Test
    @DisplayName("Should return 15 for large round amount of 50000")
    void roundAmount50000_returns15() {
        assertEquals(15, rule.calculateRiskScore(buildEvent("50000")));
    }

    @Test
    @DisplayName("Should return 0 for non-round amount")
    void nonRoundAmount_returnsZero() {
        assertEquals(0, rule.calculateRiskScore(buildEvent("1500")));
    }

    @Test
    @DisplayName("Should return 0 for small round amount below 1000")
    void smallRoundAmount_returnsZero() {
        assertEquals(0, rule.calculateRiskScore(buildEvent("500")));
    }

    @Test
    @DisplayName("Should return 0 for amount with decimals")
    void decimalAmount_returnsZero() {
        assertEquals(0, rule.calculateRiskScore(buildEvent("1000.50")));
    }

    @Test
    @DisplayName("Rule name should be RoundAmountRule")
    void ruleName() {
        assertEquals("RoundAmountRule", rule.getRuleName());
    }

    private TransactionEventDTO buildEvent(String amount) {
        return TransactionEventDTO.builder()
                .transactionId("TX-001")
                .senderAccountId("ACC-001")
                .receiverAccountId("ACC-002")
                .amount(new BigDecimal(amount))
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
