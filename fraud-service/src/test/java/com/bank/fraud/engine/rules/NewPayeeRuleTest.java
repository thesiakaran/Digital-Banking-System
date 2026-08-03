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
class NewPayeeRuleTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    private NewPayeeRule rule;

    @BeforeEach
    void setUp() {
        rule = new NewPayeeRule(redisTemplate);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("Should return 30 for new payee")
    void newPayee_returns30() {
        when(setOperations.isMember("known_payees:account:ACC-001", "ACC-002")).thenReturn(false);
        assertEquals(30, rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002")));
    }

    @Test
    @DisplayName("Should return 0 for known payee")
    void knownPayee_returnsZero() {
        when(setOperations.isMember("known_payees:account:ACC-001", "ACC-002")).thenReturn(true);
        assertEquals(0, rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002")));
    }

    @Test
    @DisplayName("Should add new payee to known set")
    void newPayee_addsToKnownSet() {
        when(setOperations.isMember("known_payees:account:ACC-001", "ACC-002")).thenReturn(false);
        rule.calculateRiskScore(buildEvent("ACC-001", "ACC-002"));
        verify(setOperations).add("known_payees:account:ACC-001", "ACC-002");
    }

    @Test
    @DisplayName("Should return 0 when sender is null")
    void nullSender_returnsZero() {
        assertEquals(0, rule.calculateRiskScore(buildEvent(null, "ACC-002")));
    }

    @Test
    @DisplayName("Should return 0 when sender is CASH")
    void cashSender_returnsZero() {
        assertEquals(0, rule.calculateRiskScore(buildEvent("CASH", "ACC-002")));
    }

    @Test
    @DisplayName("Rule name should be NewPayeeRule")
    void ruleName() {
        assertEquals("NewPayeeRule", rule.getRuleName());
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
