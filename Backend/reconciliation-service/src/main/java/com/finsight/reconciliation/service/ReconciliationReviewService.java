package com.finsight.reconciliation.service;

import com.finsight.common.dto.PagedResponse;
import com.finsight.reconciliation.dto.request.ReviewDecisionRequest;
import com.finsight.reconciliation.dto.response.ReconciliationDiscrepancyResponse;
import com.finsight.reconciliation.dto.response.ReviewDecisionResponse;
import com.finsight.reconciliation.enums.CaseStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReconciliationReviewService {
    ReviewDecisionResponse submitDecision(Long caseId, ReviewDecisionRequest request, String reviewerId);
    PagedResponse<ReconciliationDiscrepancyResponse> getDiscrepancies(CaseStatus status, Pageable pageable);
    ReconciliationDiscrepancyResponse getDiscrepancyById(Long caseId);
    List<ReviewDecisionResponse> getDecisionsByCaseId(Long caseId);
}
