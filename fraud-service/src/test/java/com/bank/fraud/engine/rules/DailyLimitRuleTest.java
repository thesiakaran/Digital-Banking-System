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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyLimitRuleTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private DailyLimitRule rule;

    @BeforeEach
    void setUp() {
        rule = new DailyLimitRule(redisTemplate);
        ReflectionTestUtils.setField(rule, "maxDailyAmount", new BigDecimal("15000"));
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should return 0 when daily total is within limit")
    void withinLimit_returnsZero() {
        when(valueOperations.increment(anyString(), anyDouble())).thenReturn(8000.0);
        assertEquals(0, rule.calculateRiskScore(buildEvent("ACC-001", "3000")));
    }

    @Test
    @DisplayName("Should return 100 when daily limit is breached")
    void limitBreached_returns100() {
        when(valueOperations.increment(anyString(), anyDouble())).thenReturn(17000.0);
        assertEquals(100, rule.calculateRiskScore(buildEvent("ACC-001", "5000")));
    }

    @Test
    @DisplayName("Should return 0 for first transaction of the day and set TTL")
    void firstTransaction_returnsZero() {
        when(valueOperations.increment(anyString(), eq(5000.0))).thenReturn(5000.0);
        assertEquals(0, rule.calculateRiskScore(buildEvent("ACC-001", "5000")));
        verify(redisTemplate).expire(anyString(), eq(Duration.ofHours(24)));
    }

    @Test
    @DisplayName("Should return 0 when sender is null")
    void nullSender_returnsZero() {
        assertEquals(0, rule.calculateRiskScore(buildEvent(null, "5000")));
    }

    @Test
    @DisplayName("Should return 0 when sender is CASH")
    void cashSender_returnsZero() {
        assertEquals(0, rule.calculateRiskScore(buildEvent("CASH", "5000")));
    }

    @Test
    @DisplayName("Rule name should be DailyLimitRule")
    void ruleName() {
        assertEquals("DailyLimitRule", rule.getRuleName());
    }

    private TransactionEventDTO buildEvent(String sender, String amount) {
        return TransactionEventDTO.builder()
                .transactionId("TX-001")
                .senderAccountId(sender)
                .receiverAccountId("ACC-002")
                .amount(new BigDecimal(amount))
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
