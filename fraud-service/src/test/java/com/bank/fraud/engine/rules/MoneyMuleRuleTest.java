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

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoneyMuleRuleTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private MoneyMuleRule rule;

    @BeforeEach
    void setUp() {
        rule = new MoneyMuleRule(redisTemplate);
    }

    @Test
    @DisplayName("Should return 50 when sender recently received large sum")
    void recentlyReceivedLargeSum_returns50() {
        when(redisTemplate.hasKey("recent_incoming:account:ACC-001")).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        assertEquals(50, rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002", "5000")));
    }

    @Test
    @DisplayName("Should return 0 when sender has no recent incoming")
    void noRecentIncoming_returnsZero() {
        when(redisTemplate.hasKey("recent_incoming:account:ACC-001")).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        assertEquals(0, rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002", "5000")));
    }

    @Test
    @DisplayName("Should return 0 when sender is null")
    void nullSender_returnsZero() {
        assertEquals(0, rule.calculateRiskScore(buildEvent(null, "ACC-002", "5000")));
    }

    @Test
    @DisplayName("Should return 0 when sender is CASH")
    void cashSender_returnsZero() {
        assertEquals(0, rule.calculateRiskScore(buildEvent("CASH", "ACC-002", "5000")));
    }

    @Test
    @DisplayName("Should track receiver for large amounts")
    void largeAmount_tracksReceiver() {
        when(redisTemplate.hasKey("recent_incoming:account:ACC-001")).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002", "1000"));
        verify(valueOperations).set("recent_incoming:account:ACC-002", "true", Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("Should NOT track receiver for small amounts")
    void smallAmount_doesNotTrackReceiver() {
        when(redisTemplate.hasKey("recent_incoming:account:ACC-001")).thenReturn(false);
        assertEquals(0, rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002", "500")));
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("Rule name should be MoneyMuleRule")
    void ruleName() {
        assertEquals("MoneyMuleRule", rule.getRuleName());
    }

    private TransactionEventDTO buildEvent(String sender, String receiver, String amount) {
        return TransactionEventDTO.builder()
                .transactionId("TX-001")
                .senderAccountId(sender)
                .receiverAccountId(receiver)
                .amount(new BigDecimal(amount))
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
