package com.finsight.reconciliation.controller;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import com.finsight.reconciliation.dto.request.ExecuteReconciliationRequest;
import com.finsight.reconciliation.dto.response.ReconciliationDiscrepancyResponse;
import com.finsight.reconciliation.dto.response.ReconciliationMatchResponse;
import com.finsight.reconciliation.dto.response.ReconciliationRunResponse;
import com.finsight.reconciliation.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
@Tag(name = "Reconciliation Operations", description = "Endpoints for executing and inspecting reconciliation runs")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @PostMapping("/runs")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Execute reconciliation batch", description = "Triggers deterministic exact, tolerance, and 1-to-many transaction matching")
    public ResponseEntity<ApiResponse<ReconciliationRunResponse>> executeReconciliation(
            @Valid @RequestBody(required = false) ExecuteReconciliationRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "SYSTEM";
        ExecuteReconciliationRequest req = request != null ? request : new ExecuteReconciliationRequest();
        ReconciliationRunResponse response = reconciliationService.executeReconciliation(req, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Reconciliation run completed successfully"));
    }

    @GetMapping("/runs")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR')")
    @Operation(summary = "List reconciliation runs", description = "Returns paginated list of historic reconciliation runs")
    public ResponseEntity<ApiResponse<PagedResponse<ReconciliationRunResponse>>> listRuns(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(reconciliationService.listRuns(pageable), "Reconciliation runs retrieved"));
    }

    @GetMapping("/runs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR')")
    @Operation(summary = "Get run details by ID", description = "Returns detailed statistics and status of a specific reconciliation run")
    public ResponseEntity<ApiResponse<ReconciliationRunResponse>> getRunById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(reconciliationService.getRunById(id), "Reconciliation run details retrieved"));
    }

    @GetMapping("/runs/{id}/matches")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR')")
    @Operation(summary = "Get matches for run", description = "Returns paginated list of matched records for a specific run")
    public ResponseEntity<ApiResponse<PagedResponse<ReconciliationMatchResponse>>> getMatchesByRun(
            @PathVariable("id") Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(reconciliationService.getMatchesByRun(id, pageable), "Matches retrieved"));
    }

    @GetMapping("/runs/{id}/discrepancies")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR')")
    @Operation(summary = "Get discrepancies for run", description = "Returns paginated list of discrepancy cases generated in a specific run")
    public ResponseEntity<ApiResponse<PagedResponse<ReconciliationDiscrepancyResponse>>> getDiscrepanciesByRun(
            @PathVariable("id") Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(reconciliationService.getDiscrepanciesByRun(id, pageable), "Discrepancies retrieved"));
    }
}
