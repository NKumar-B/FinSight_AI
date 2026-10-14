package com.finsight.reconciliation.controller;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import com.finsight.reconciliation.dto.request.ReviewDecisionRequest;
import com.finsight.reconciliation.dto.response.ReconciliationDiscrepancyResponse;
import com.finsight.reconciliation.dto.response.ReviewDecisionResponse;
import com.finsight.reconciliation.enums.CaseStatus;
import com.finsight.reconciliation.service.ReconciliationReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reconciliation/review")
@RequiredArgsConstructor
@Tag(name = "Reconciliation Review", description = "Endpoints for discrepancy case review, approval, adjustment, and write-off decisions")
public class ReconciliationReviewController {

    private final ReconciliationReviewService reviewService;

    @PostMapping("/cases/{caseId}/decide")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'FINANCIAL_ANALYST')")
    @Operation(summary = "Submit review decision", description = "Record human reviewer decision on a reconciliation discrepancy case")
    public ResponseEntity<ApiResponse<ReviewDecisionResponse>> submitDecision(
            @PathVariable("caseId") Long caseId,
            @Valid @RequestBody ReviewDecisionRequest request,
            Authentication authentication) {
        String reviewer = authentication != null ? authentication.getName() : "ANONYMOUS_REVIEWER";
        ReviewDecisionResponse response = reviewService.submitDecision(caseId, request, reviewer);
        return ResponseEntity.ok(ApiResponse.ok(response, "Review decision recorded successfully"));
    }

    @GetMapping("/cases")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'FINANCIAL_ANALYST', 'AUDITOR')")
    @Operation(summary = "List discrepancy cases", description = "Returns paginated list of discrepancy cases, optionally filtered by status")
    public ResponseEntity<ApiResponse<PagedResponse<ReconciliationDiscrepancyResponse>>> listDiscrepancies(
            @RequestParam(name = "status", required = false) CaseStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getDiscrepancies(status, pageable), "Discrepancy cases retrieved"));
    }

    @GetMapping("/cases/{caseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'FINANCIAL_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get discrepancy case by ID", description = "Returns full details of a single discrepancy case")
    public ResponseEntity<ApiResponse<ReconciliationDiscrepancyResponse>> getDiscrepancyById(
            @PathVariable("caseId") Long caseId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getDiscrepancyById(caseId), "Discrepancy case retrieved"));
    }

    @GetMapping("/cases/{caseId}/decisions")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'FINANCIAL_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get decision history for case", description = "Returns audit trail of decisions made on this case")
    public ResponseEntity<ApiResponse<List<ReviewDecisionResponse>>> getDecisionsByCaseId(
            @PathVariable("caseId") Long caseId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getDecisionsByCaseId(caseId), "Decision history retrieved"));
    }
}
