package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class NewPayeeRule implements FraudRule {

    private final RedisTemplate<String, String> redisTemplate;

    public NewPayeeRule(@org.springframework.beans.factory.annotation.Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public int calculateRiskScore(TransactionEventDTO event) {
        if (event.getSenderAccountId() == null || "CASH".equals(event.getSenderAccountId())) {
            return 0;
        }

        String key = "known_payees:account:" + event.getSenderAccountId();
        
        Boolean isKnown = redisTemplate.opsForSet().isMember(key, event.getReceiverAccountId());
        
        if (Boolean.FALSE.equals(isKnown)) {
            // Add them for the future, but flag this first transaction
            redisTemplate.opsForSet().add(key, event.getReceiverAccountId());
            return 30; // 30 points for sending to a stranger
        }
        
        return 0;
    }

    @Override
    public String getRuleName() {
        return "NewPayeeRule";
    }
}
