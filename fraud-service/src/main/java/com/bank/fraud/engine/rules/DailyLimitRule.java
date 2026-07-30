package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

@Component
@Slf4j
public class DailyLimitRule {

    private final RedisTemplate<String, String> redisTemplate;

    public DailyLimitRule(@org.springframework.beans.factory.annotation.Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${fraud.rules.dailylimit.max-amount:15000}")
    private BigDecimal maxDailyAmount;

    public boolean isFraud(TransactionEventDTO event) {
        String accountId = event.getSenderAccountId();
        if (accountId == null || accountId.isEmpty() || "CASH".equals(accountId)) {
            return false; // Deposits or system transfers don't count towards sending daily limit
        }

        String today = LocalDate.now().toString();
        String key = "dailylimit:account:" + accountId + ":" + today;
        
        // Increment daily total in Redis
        Double currentTotal = redisTemplate.opsForValue().increment(key, event.getAmount().doubleValue());
        
        // If this is the first transaction of the day, set expiration to 24 hours
        if (currentTotal != null && currentTotal.equals(event.getAmount().doubleValue())) {
            redisTemplate.expire(key, Duration.ofHours(24));
        }

        if (currentTotal != null && BigDecimal.valueOf(currentTotal).compareTo(maxDailyAmount) > 0) {
            log.warn("Daily Limit breach for account {}: attempted total is ${}, limit is ${}", 
                     accountId, currentTotal, maxDailyAmount);
            return true;
        }
        return false;
    }
}
