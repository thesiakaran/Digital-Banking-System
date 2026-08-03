package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ThresholdRuleTest {

    private ThresholdRule rule;

    @BeforeEach
    void setUp() {
        rule = new ThresholdRule();
        ReflectionTestUtils.setField(rule, "highAmount", new BigDecimal("10000"));
        ReflectionTestUtils.setField(rule, "maxAmount", new BigDecimal("100000"));
    }

    @Test
    @DisplayName("Should return 0 for normal amount below high threshold")
    void normalAmount_returnsZero() {
        TransactionEventDTO event = buildEvent("5000");
        assertEquals(0, rule.calculateRiskScore(event));
    }

    @Test
    @DisplayName("Should return 30 points for high amount above 10000")
    void highAmount_returns30() {
        TransactionEventDTO event = buildEvent("15000");
        assertEquals(30, rule.calculateRiskScore(event));
    }

    @Test
    @DisplayName("Should return 100 points (instant block) for amount exceeding max")
    void maxAmount_returns100() {
        TransactionEventDTO event = buildEvent("150000");
        assertEquals(100, rule.calculateRiskScore(event));
    }

    @Test
    @DisplayName("Should return 0 for amount exactly at high threshold")
    void exactHighThreshold_returnsZero() {
        TransactionEventDTO event = buildEvent("10000");
        assertEquals(0, rule.calculateRiskScore(event));
    }

    @Test
    @DisplayName("Should return 30 for amount exactly at max threshold")
    void exactMaxThreshold_returns30() {
        TransactionEventDTO event = buildEvent("100000");
        assertEquals(30, rule.calculateRiskScore(event));
    }

    @Test
    @DisplayName("Rule name should be ThresholdRule")
    void ruleName() {
        assertEquals("ThresholdRule", rule.getRuleName());
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
