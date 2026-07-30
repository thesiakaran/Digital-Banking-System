package com.bank.transaction.repository;

import com.bank.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    Optional<Transaction> findByTransactionId(String transactionId);
    java.util.List<Transaction> findBySenderAccountNumberOrReceiverAccountNumberOrderByCreatedAtDesc(String sender, String receiver);
}
