package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VelocityRuleTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private VelocityRule rule;

    @BeforeEach
    void setUp() {
        rule = new VelocityRule(redisTemplate);
        ReflectionTestUtils.setField(rule, "maxCount", 5);
        ReflectionTestUtils.setField(rule, "timeWindowSeconds", 60L);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should return 0 when transaction count is within limit")
    void withinLimit_returnsZero() {
        when(valueOperations.increment("velocity:account:ACC-001")).thenReturn(3L);
        TransactionEventDTO event = buildEvent("ACC-001", "ACC-002");
        assertEquals(0, rule.calculateRiskScore(event));
    }

    @Test
    @DisplayName("Should return 40 when velocity limit is breached")
    void velocityBreach_returns40() {
        when(valueOperations.increment("velocity:account:ACC-001")).thenReturn(6L);
        TransactionEventDTO event = buildEvent("ACC-001", "ACC-002");
        assertEquals(40, rule.calculateRiskScore(event));
    }

    @Test
    @DisplayName("Should set TTL on first transaction")
    void firstTransaction_setsTTL() {
        when(valueOperations.increment("velocity:account:ACC-001")).thenReturn(1L);
        rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002"));
        verify(redisTemplate).expire("velocity:account:ACC-001", Duration.ofSeconds(60));
    }

    @Test
    @DisplayName("Should use receiverAccountId when sender is null")
    void nullSender_usesReceiver() {
        when(valueOperations.increment("velocity:account:ACC-002")).thenReturn(1L);
        TransactionEventDTO event = buildEvent(null, "ACC-002");
        rule.calculateRiskScore(event);
        verify(valueOperations).increment("velocity:account:ACC-002");
    }

    @Test
    @DisplayName("Should use receiverAccountId when sender is CASH")
    void cashSender_usesReceiver() {
        when(valueOperations.increment("velocity:account:ACC-002")).thenReturn(1L);
        TransactionEventDTO event = buildEvent("CASH", "ACC-002");
        rule.calculateRiskScore(event);
        verify(valueOperations).increment("velocity:account:ACC-002");
    }

    @Test
    @DisplayName("Rule name should be VelocityRule")
    void ruleName() {
        assertEquals("VelocityRule", rule.getRuleName());
    }

    private TransactionEventDTO buildEvent(String sender, String receiver) {
        return TransactionEventDTO.builder()
                .transactionId("TX-001")
                .senderAccountId(sender)
                .receiverAccountId(receiver)
                .amount(new BigDecimal("5000"))
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
