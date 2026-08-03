package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistRuleTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    private BlacklistRule rule;

    @BeforeEach
    void setUp() {
        rule = new BlacklistRule(redisTemplate);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("Should return 100 when sender is blacklisted")
    void senderBlacklisted_returns100() {
        when(setOperations.isMember("blacklist:accounts", "ACC-001")).thenReturn(true);
        when(setOperations.isMember("blacklist:accounts", "ACC-002")).thenReturn(false);
        assertEquals(100, rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002")));
    }

    @Test
    @DisplayName("Should return 100 when receiver is blacklisted")
    void receiverBlacklisted_returns100() {
        when(setOperations.isMember("blacklist:accounts", "ACC-001")).thenReturn(false);
        when(setOperations.isMember("blacklist:accounts", "ACC-002")).thenReturn(true);
        assertEquals(100, rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002")));
    }

    @Test
    @DisplayName("Should return 0 when neither is blacklisted")
    void noneBlacklisted_returnsZero() {
        when(setOperations.isMember("blacklist:accounts", "ACC-001")).thenReturn(false);
        when(setOperations.isMember("blacklist:accounts", "ACC-002")).thenReturn(false);
        assertEquals(0, rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002")));
    }

    @Test
    @DisplayName("Should handle null sender gracefully")
    void nullSender_handlesGracefully() {
        when(setOperations.isMember("blacklist:accounts", "ACC-002")).thenReturn(false);
        assertEquals(0, rule.calculateRiskScore(buildEvent(null, "ACC-002")));
    }

    @Test
    @DisplayName("Rule name should be BlacklistRule")
    void ruleName() {
        assertEquals("BlacklistRule", rule.getRuleName());
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
