package com.bank.fraud.repository;

import com.bank.fraud.entity.AlertStatus;
import com.bank.fraud.entity.FraudAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    Page<FraudAlert> findAllByOrderByDetectedAtDesc(Pageable pageable);
    Page<FraudAlert> findByStatusOrderByDetectedAtDesc(AlertStatus status, Pageable pageable);
    Optional<FraudAlert> findByTransactionId(String transactionId);
    List<FraudAlert> findBySenderAccountId(String senderAccountId);
    long countByStatus(AlertStatus status);

    @Query("SELECT fa.senderAccountId, COUNT(fa) as cnt FROM FraudAlert fa GROUP BY fa.senderAccountId ORDER BY cnt DESC")
    List<Object[]> findTopFlaggedAccounts(Pageable pageable);
}
