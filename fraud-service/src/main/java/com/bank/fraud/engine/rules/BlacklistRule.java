package com.bank.fraud.engine.rules;

import com.bank.fraud.dto.event.TransactionEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class BlacklistRule {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_KEY = "blacklist:accounts";

    public BlacklistRule(@org.springframework.beans.factory.annotation.Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isFraud(TransactionEventDTO event) {
        Boolean isSenderBlacklisted = false;
        if (event.getSenderAccountId() != null && !event.getSenderAccountId().isEmpty()) {
            isSenderBlacklisted = redisTemplate.opsForSet().isMember(BLACKLIST_KEY, event.getSenderAccountId());
        }
        Boolean isReceiverBlacklisted = redisTemplate.opsForSet().isMember(BLACKLIST_KEY, event.getReceiverAccountId());
        
        return Boolean.TRUE.equals(isSenderBlacklisted) || Boolean.TRUE.equals(isReceiverBlacklisted);
    }
}
