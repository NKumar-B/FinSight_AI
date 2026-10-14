package com.finsight.reconciliation.mapper;

import com.finsight.reconciliation.dto.response.ReconciliationDiscrepancyResponse;
import com.finsight.reconciliation.dto.response.ReconciliationMatchResponse;
import com.finsight.reconciliation.dto.response.ReconciliationRunResponse;
import com.finsight.reconciliation.dto.response.ReviewDecisionResponse;
import com.finsight.reconciliation.entity.ReconciliationDiscrepancy;
import com.finsight.reconciliation.entity.ReconciliationMatch;
import com.finsight.reconciliation.entity.ReconciliationRun;
import com.finsight.reconciliation.entity.ReviewDecision;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationMapper {

    public ReconciliationRunResponse toRunResponse(ReconciliationRun run) {
        if (run == null) return null;
        return ReconciliationRunResponse.builder()
                .id(run.getId())
                .runReference(run.getRunReference())
                .status(run.getStatus())
                .totalSourceRecords(run.getTotalSourceRecords())
                .totalTargetRecords(run.getTotalTargetRecords())
                .exactMatchesCount(run.getExactMatchesCount())
                .toleranceMatchesCount(run.getToleranceMatchesCount())
                .discrepanciesCount(run.getDiscrepanciesCount())
                .unmatchedCount(run.getUnmatchedCount())
                .dateToleranceDays(run.getDateToleranceDays())
                .amountTolerance(run.getAmountTolerance())
                .executedBy(run.getExecutedBy())
                .createdAt(run.getCreatedAt())
                .completedAt(run.getCompletedAt())
                .build();
    }

    public ReconciliationMatchResponse toMatchResponse(ReconciliationMatch match) {
        if (match == null) return null;
        return ReconciliationMatchResponse.builder()
                .id(match.getId())
                .runId(match.getReconciliationRun() != null ? match.getReconciliationRun().getId() : null)
                .sourceTransactionId(match.getSourceTransactionId())
                .sourceTransactionRef(match.getSourceTransactionRef())
                .targetTransactionId(match.getTargetTransactionId())
                .targetTransactionRef(match.getTargetTransactionRef())
                .targetTransactionIds(match.getTargetTransactionIds())
                .matchType(match.getMatchType())
                .amountDifference(match.getAmountDifference())
                .dateDifferenceDays(match.getDateDifferenceDays())
                .status(match.getStatus())
                .reviewedBy(match.getReviewedBy())
                .reviewNotes(match.getReviewNotes())
                .createdAt(match.getCreatedAt())
                .build();
    }

    public ReconciliationDiscrepancyResponse toDiscrepancyResponse(ReconciliationDiscrepancy discrepancy) {
        if (discrepancy == null) return null;
        return ReconciliationDiscrepancyResponse.builder()
                .id(discrepancy.getId())
                .runId(discrepancy.getReconciliationRun() != null ? discrepancy.getReconciliationRun().getId() : null)
                .caseReference(discrepancy.getCaseReference())
                .discrepancyType(discrepancy.getDiscrepancyType())
                .sourceTransactionId(discrepancy.getSourceTransactionId())
                .sourceTransactionRef(discrepancy.getSourceTransactionRef())
                .targetTransactionId(discrepancy.getTargetTransactionId())
                .targetTransactionRef(discrepancy.getTargetTransactionRef())
                .sourceAmount(discrepancy.getSourceAmount())
                .targetAmount(discrepancy.getTargetAmount())
                .amountDiscrepancy(discrepancy.getAmountDiscrepancy())
                .sourceDate(discrepancy.getSourceDate())
                .targetDate(discrepancy.getTargetDate())
                .status(discrepancy.getStatus())
                .assignedTo(discrepancy.getAssignedTo())
                .resolutionNotes(discrepancy.getResolutionNotes())
                .createdAt(discrepancy.getCreatedAt())
                .resolvedAt(discrepancy.getResolvedAt())
                .build();
    }

    public ReviewDecisionResponse toDecisionResponse(ReviewDecision decision) {
        if (decision == null) return null;
        return ReviewDecisionResponse.builder()
                .id(decision.getId())
                .caseId(decision.getCaseId())
                .matchId(decision.getMatchId())
                .decision(decision.getDecision())
                .reviewerId(decision.getReviewerId())
                .reason(decision.getReason())
                .beforeState(decision.getBeforeState())
                .afterState(decision.getAfterState())
                .createdAt(decision.getCreatedAt())
                .build();
    }
}
