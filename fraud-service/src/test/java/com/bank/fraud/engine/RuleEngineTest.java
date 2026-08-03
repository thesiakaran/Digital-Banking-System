package com.bank.fraud.engine;

import com.bank.fraud.dto.event.FraudResultEventDTO;
import com.bank.fraud.dto.event.TransactionEventDTO;
import com.bank.fraud.engine.rules.FraudRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RuleEngineTest {

    private RuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngine(Collections.emptyList());
        ReflectionTestUtils.setField(ruleEngine, "maxRiskScore", 100);
    }

    @Test
    @DisplayName("Should approve when no rules trigger")
    void noRulesTrigger_approved() {
        FraudRule rule1 = mockRule("Rule1", 0);
        FraudRule rule2 = mockRule("Rule2", 0);
        ruleEngine = new RuleEngine(Arrays.asList(rule1, rule2));
        ReflectionTestUtils.setField(ruleEngine, "maxRiskScore", 100);

        FraudResultEventDTO result = ruleEngine.evaluate(buildEvent());
        assertTrue(result.getApproved());
    }

    @Test
    @DisplayName("Should approve when total score is below threshold")
    void belowThreshold_approved() {
        FraudRule rule1 = mockRule("Rule1", 30);
        FraudRule rule2 = mockRule("Rule2", 40);
        ruleEngine = new RuleEngine(Arrays.asList(rule1, rule2));
        ReflectionTestUtils.setField(ruleEngine, "maxRiskScore", 100);

        FraudResultEventDTO result = ruleEngine.evaluate(buildEvent());
        assertTrue(result.getApproved());
        assertTrue(result.getReason().contains("70"));
    }

    @Test
    @DisplayName("Should reject when total score meets threshold")
    void meetsThreshold_rejected() {
        FraudRule rule1 = mockRule("Rule1", 50);
        FraudRule rule2 = mockRule("Rule2", 50);
        ruleEngine = new RuleEngine(Arrays.asList(rule1, rule2));
        ReflectionTestUtils.setField(ruleEngine, "maxRiskScore", 100);

        FraudResultEventDTO result = ruleEngine.evaluate(buildEvent());
        assertFalse(result.getApproved());
    }

    @Test
    @DisplayName("Should reject when total score exceeds threshold")
    void exceedsThreshold_rejected() {
        FraudRule rule1 = mockRule("InstantBlock", 100);
        ruleEngine = new RuleEngine(Collections.singletonList(rule1));
        ReflectionTestUtils.setField(ruleEngine, "maxRiskScore", 100);

        FraudResultEventDTO result = ruleEngine.evaluate(buildEvent());
        assertFalse(result.getApproved());
        assertTrue(result.getReason().contains("REJECTED"));
    }

    @Test
    @DisplayName("Should include triggered rule names in reason")
    void triggeredRuleNames_inReason() {
        FraudRule rule1 = mockRule("VelocityRule", 40);
        FraudRule rule2 = mockRule("ThresholdRule", 30);
        ruleEngine = new RuleEngine(Arrays.asList(rule1, rule2));
        ReflectionTestUtils.setField(ruleEngine, "maxRiskScore", 100);

        FraudResultEventDTO result = ruleEngine.evaluate(buildEvent());
        assertTrue(result.getReason().contains("VelocityRule"));
        assertTrue(result.getReason().contains("ThresholdRule"));
    }

    @Test
    @DisplayName("Result should contain transaction ID and evaluatedAt timestamp")
    void resultContainsMetadata() {
        ruleEngine = new RuleEngine(Collections.emptyList());
        ReflectionTestUtils.setField(ruleEngine, "maxRiskScore", 100);

        FraudResultEventDTO result = ruleEngine.evaluate(buildEvent());
        assertEquals("TX-001", result.getTransactionId());
        assertNotNull(result.getEvaluatedAt());
    }

    private FraudRule mockRule(String name, int score) {
        FraudRule rule = mock(FraudRule.class);
        lenient().when(rule.getRuleName()).thenReturn(name);
        lenient().when(rule.calculateRiskScore(any())).thenReturn(score);
        return rule;
    }

    private TransactionEventDTO buildEvent() {
        return TransactionEventDTO.builder()
                .transactionId("TX-001")
                .senderAccountId("ACC-001")
                .receiverAccountId("ACC-002")
                .amount(new BigDecimal("5000"))
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
