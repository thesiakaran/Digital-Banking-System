package com.bank.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 20)
    private String accountnumber;

    @Column(nullable = false)
    private String accountholdername;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accounttype;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(precision = 15, scale = 2)
    private BigDecimal dailylimit;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdat;

    @UpdateTimestamp
    private LocalDateTime updatedat;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdat = LocalDateTime.now();
        this.updatedat = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedat = LocalDateTime.now();
    }
}
