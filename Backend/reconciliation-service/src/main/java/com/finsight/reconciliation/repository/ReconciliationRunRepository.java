package com.finsight.reconciliation.repository;

import com.finsight.reconciliation.entity.ReconciliationRun;
import com.finsight.reconciliation.enums.ReconciliationRunStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, Long> {
    Optional<ReconciliationRun> findByRunReference(String runReference);
    Page<ReconciliationRun> findByStatus(ReconciliationRunStatus status, Pageable pageable);
}
