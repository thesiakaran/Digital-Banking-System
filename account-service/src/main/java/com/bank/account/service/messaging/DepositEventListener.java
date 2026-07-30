package com.bank.account.service.messaging;

import com.bank.account.dto.event.DepositEventDTO;
import com.bank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositEventListener {

    private final AccountService accountService;

    @KafkaListener(topics = "deposit-events", groupId = "account-service-group")
    public void handleDepositEvent(DepositEventDTO event) {
        log.info("Received DepositEvent for account: {} amount: {}", event.getReceiverAccountId(), event.getAmount());
        try {
            accountService.deposit(event.getReceiverAccountId(), event.getAmount());
            log.info("Successfully processed deposit for transaction {}", event.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to process deposit event for transaction {}", event.getTransactionId(), e);
        }
    }
}
