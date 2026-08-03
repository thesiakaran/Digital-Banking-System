package com.bank.fraud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import redis.embedded.RedisServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Configuration
public class EmbeddedInfrastructureConfig {

    private RedisServer redisServer;

    @Bean
    public EmbeddedKafkaBroker embeddedKafka() {
        // Start an embedded Kafka broker on port 9092
        return new EmbeddedKafkaBroker(1, true, 1, "transaction-events")
                .kafkaPorts(9092);
    }

    @PostConstruct
    public void startRedis() {
        try {
            // Start embedded Redis on port 6379
            redisServer = new RedisServer(6379);
            redisServer.start();
        } catch (Exception e) {
            System.err.println("Failed to start Embedded Redis: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
