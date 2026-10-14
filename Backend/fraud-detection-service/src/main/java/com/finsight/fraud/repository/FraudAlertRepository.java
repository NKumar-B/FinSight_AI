package com.finsight.fraud.repository;

import com.finsight.fraud.entity.FraudAlert;
import com.finsight.fraud.enums.AlertStatus;
import com.finsight.fraud.enums.RiskSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {

    Optional<FraudAlert> findByAlertReference(String alertReference);

    Page<FraudAlert> findByStatus(AlertStatus status, Pageable pageable);

    Page<FraudAlert> findBySeverity(RiskSeverity severity, Pageable pageable);

    Page<FraudAlert> findByStatusAndSeverity(AlertStatus status, RiskSeverity severity, Pageable pageable);

    Page<FraudAlert> findByAccountId(Long accountId, Pageable pageable);

    long countByStatus(AlertStatus status);

    long countBySeverity(RiskSeverity severity);

    @Query("SELECT COUNT(a) FROM FraudAlert a WHERE a.status = 'NEW'")
    long countNewAlerts();
}
