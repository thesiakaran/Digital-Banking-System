package com.bank.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise Fallback Controller for Resilience4j Circuit Breaker
 * Handles gracefully degrading the system when a microservice crashes.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/transaction")
    public ResponseEntity<Map<String, Object>> transactionFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Transaction Service Unavailable");
        response.put("message", "The Transaction Microservice is currently offline. The Circuit Breaker has tripped to prevent cascading failures. Please try again later.");
        
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @RequestMapping("/fraud")
    public ResponseEntity<Map<String, Object>> fraudFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Fraud Service Unavailable");
        response.put("message", "The Fraud Detection Engine is currently offline. The Circuit Breaker has tripped to ensure core banking operations remain stable.");
        
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
