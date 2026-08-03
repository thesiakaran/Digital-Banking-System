package com.bank.gateway.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${jwt.secret:4a362140a1b5c46430b809a4d2e8b0b8c0a8e9e7f5d4c3b2a19876543210}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @PostMapping("/login")
    public Mono<Map<String, String>> login(@RequestBody Map<String, String> request) {
        // In a real app, you would validate credentials against a database here.
        // For the demo, we just issue a token.
        String username = request.getOrDefault("username", "demo_user");
        
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 3600000; // 1 hour
        Date exp = new Date(expMillis);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(new Date(nowMillis))
                .expiration(exp)
                .signWith(getSigningKey())
                .compact();

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return Mono.just(response);
    }
}
