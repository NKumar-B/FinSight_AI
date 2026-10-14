package com.finsight.reconciliation.repository;

import com.finsight.reconciliation.entity.ReconciliationMatch;
import com.finsight.reconciliation.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationMatchRepository extends JpaRepository<ReconciliationMatch, Long> {
    Page<ReconciliationMatch> findByReconciliationRunId(Long runId, Pageable pageable);
    List<ReconciliationMatch> findByReconciliationRunId(Long runId);
    Page<ReconciliationMatch> findByStatus(MatchStatus status, Pageable pageable);
}
