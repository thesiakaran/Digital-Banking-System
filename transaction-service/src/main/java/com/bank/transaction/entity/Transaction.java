package com.bank.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", unique = true, nullable = false, length = 36)
    private String transactionId;

    @Column(name = "idempotency_key", unique = true, nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "sender_account_number", nullable = true, length = 20)
    private String senderAccountNumber;

    @Column(name = "receiver_account_number", nullable = true, length = 20)
    private String receiverAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransactionStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.transactionId == null) {
            this.transactionId = UUID.randomUUID().toString();
        }
    }
}
