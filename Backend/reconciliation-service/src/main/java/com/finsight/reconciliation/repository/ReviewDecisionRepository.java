package com.finsight.reconciliation.repository;

import com.finsight.reconciliation.entity.ReviewDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewDecisionRepository extends JpaRepository<ReviewDecision, Long> {
    List<ReviewDecision> findByCaseId(Long caseId);
    List<ReviewDecision> findByMatchId(Long matchId);
}
