package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class VelocityRule implements FraudRule {

    private final RedisTemplate<String, String> redisTemplate;

    public VelocityRule(@org.springframework.beans.factory.annotation.Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${fraud.rules.velocity.max-count:3}")
    private int maxCount;

    @Value("${fraud.rules.velocity.time-window-seconds:60}")
    private long timeWindowSeconds;

    @Override
    public int calculateRiskScore(TransactionEventDTO event) {
        String accountId = event.getSenderAccountId();
        if (accountId == null || accountId.isEmpty() || "CASH".equals(accountId)) {
            accountId = event.getReceiverAccountId();
        }
        
        String key = "velocity:account:" + accountId;
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(timeWindowSeconds));
        }

        if (count != null && count > maxCount) {
            log.warn("Velocity breach for account {}: count is {}", event.getSenderAccountId(), count);
            return 40; // 40 Risk Points for high velocity
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "VelocityRule";
    }
}
