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
public class DailyLimitRule implements FraudRule {

    private final RedisTemplate<String, String> redisTemplate;

    public DailyLimitRule(@org.springframework.beans.factory.annotation.Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${fraud.rules.dailylimit.max-amount:15000}")
    private BigDecimal maxDailyAmount;

    @Override
    public int calculateRiskScore(TransactionEventDTO event) {
        String accountId = event.getSenderAccountId();
        if (accountId == null || accountId.isEmpty() || "CASH".equals(accountId)) {
            return 0; 
        }

        String today = LocalDate.now().toString();
        String key = "dailylimit:account:" + accountId + ":" + today;
        
        String existing = redisTemplate.opsForValue().get(key);
        BigDecimal previousTotal = (existing != null) ? new BigDecimal(existing) : BigDecimal.ZERO;
        BigDecimal currentTotal = previousTotal.add(event.getAmount());
        redisTemplate.opsForValue().set(key, currentTotal.toPlainString(), Duration.ofHours(24));

        if (currentTotal.compareTo(maxDailyAmount) > 0) {
            log.warn("Daily Limit breach for account {}: attempted total is ${}", accountId, currentTotal);
            return 100; // Instant block if daily limit breached
        }
        return 0;
    }

    @Override
    public String getRuleName() {
        return "DailyLimitRule";
    }
}
