package com.bank.fraud.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BlacklistService {
    private static final String BLACKLIST_KEY = "blacklist:accounts";
    private final RedisTemplate<String, String> redisTemplate;

    public BlacklistService(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlacklist(String accountId) {
        redisTemplate.opsForSet().add(BLACKLIST_KEY, accountId);
    }

    public void removeFromBlacklist(String accountId) {
        redisTemplate.opsForSet().remove(BLACKLIST_KEY, accountId);
    }

    public boolean isBlacklisted(String accountId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(BLACKLIST_KEY, accountId));
    }

    public Set<String> getBlacklist() {
        return redisTemplate.opsForSet().members(BLACKLIST_KEY);
    }
}
