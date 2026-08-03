package com.bank.fraud.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionId;

    private String senderAccountId;
    private String receiverAccountId;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        this.detectedAt = LocalDateTime.now();
        if (this.status == null) this.status = AlertStatus.OPEN;
        if (this.severity == null) this.severity = riskScore >= 100 ? AlertSeverity.CRITICAL : riskScore >= 50 ? AlertSeverity.HIGH : AlertSeverity.MEDIUM;
    }
}
