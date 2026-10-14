package com.finsight.reconciliation.repository;

import com.finsight.reconciliation.entity.ReconciliationDiscrepancy;
import com.finsight.reconciliation.enums.CaseStatus;
import com.finsight.reconciliation.enums.DiscrepancyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReconciliationDiscrepancyRepository extends JpaRepository<ReconciliationDiscrepancy, Long> {
    Optional<ReconciliationDiscrepancy> findByCaseReference(String caseReference);
    Page<ReconciliationDiscrepancy> findByReconciliationRunId(Long runId, Pageable pageable);
    Page<ReconciliationDiscrepancy> findByStatus(CaseStatus status, Pageable pageable);
    Page<ReconciliationDiscrepancy> findByDiscrepancyType(DiscrepancyType discrepancyType, Pageable pageable);
}
