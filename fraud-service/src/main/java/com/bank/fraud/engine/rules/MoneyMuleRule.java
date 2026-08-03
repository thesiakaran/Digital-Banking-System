package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class MoneyMuleRule implements FraudRule {

    private final RedisTemplate<String, String> redisTemplate;

    public MoneyMuleRule(@org.springframework.beans.factory.annotation.Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public int calculateRiskScore(TransactionEventDTO event) {
        if (event.getSenderAccountId() == null || "CASH".equals(event.getSenderAccountId())) {
            return 0;
        }

        // 1. Check if the current sender recently received a large sum
        String incomingKey = "recent_incoming:account:" + event.getSenderAccountId();
        Boolean recentlyReceived = redisTemplate.hasKey(incomingKey);
        
        int riskScore = 0;
        if (Boolean.TRUE.equals(recentlyReceived)) {
            riskScore = 50; // Hot potato pattern detected
        }

        // 2. Track this transaction for the receiver (if it's a large amount)
        if (event.getAmount().compareTo(new BigDecimal("1000")) >= 0) {
            String newIncomingKey = "recent_incoming:account:" + event.getReceiverAccountId();
            // Flag them as having received a large sum for 10 minutes
            redisTemplate.opsForValue().set(newIncomingKey, "true", Duration.ofMinutes(10));
        }

        return riskScore;
    }

    @Override
    public String getRuleName() {
        return "MoneyMuleRule";
    }
}
