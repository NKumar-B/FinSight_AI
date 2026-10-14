package com.finsight.fraud.controller;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import com.finsight.fraud.dto.request.EvaluateTransactionRequest;
import com.finsight.fraud.dto.request.UpdateAlertStatusRequest;
import com.finsight.fraud.dto.response.FraudAlertResponse;
import com.finsight.fraud.dto.response.FraudAlertStatsResponse;
import com.finsight.fraud.dto.response.FraudEvaluationResponse;
import com.finsight.fraud.enums.AlertStatus;
import com.finsight.fraud.enums.RiskSeverity;
import com.finsight.fraud.service.FraudDetectionService;
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
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Tag(name = "Fraud Detection", description = "Endpoints for transaction anomaly scoring, fraud alert investigation, and metrics")
public class FraudDetectionController {

    private final FraudDetectionService fraudService;

    @PostMapping("/evaluate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'FINANCIAL_ANALYST', 'FRAUD_INVESTIGATOR')")
    @Operation(summary = "Evaluate transaction risk", description = "Executes fraud detection rules (velocity, spikes, duplicates, cross-border) and returns risk score (0-100)")
    public ResponseEntity<ApiResponse<FraudEvaluationResponse>> evaluateTransaction(
            @Valid @RequestBody EvaluateTransactionRequest request) {
        FraudEvaluationResponse response = fraudService.evaluateTransaction(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response, "Transaction fraud risk evaluated successfully"));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'FRAUD_INVESTIGATOR', 'AUDITOR')")
    @Operation(summary = "List fraud alerts", description = "Returns paginated list of fraud alerts, optionally filtered by status and severity")
    public ResponseEntity<ApiResponse<PagedResponse<FraudAlertResponse>>> listAlerts(
            @RequestParam(name = "status", required = false) AlertStatus status,
            @RequestParam(name = "severity", required = false) RiskSeverity severity,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(fraudService.listAlerts(status, severity, pageable), "Fraud alerts retrieved successfully"));
    }

    @GetMapping("/alerts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'FRAUD_INVESTIGATOR', 'AUDITOR')")
    @Operation(summary = "Get fraud alert by ID", description = "Returns full details of an individual fraud alert")
    public ResponseEntity<ApiResponse<FraudAlertResponse>> getAlertById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(fraudService.getAlertById(id), "Fraud alert details retrieved"));
    }

    @PatchMapping("/alerts/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'FRAUD_INVESTIGATOR')")
    @Operation(summary = "Update fraud alert status", description = "Update status, assignment, and resolution notes on an alert during investigation")
    public ResponseEntity<ApiResponse<FraudAlertResponse>> updateAlertStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateAlertStatusRequest request,
            Authentication authentication) {
        String investigator = authentication != null ? authentication.getName() : "FRAUD_INVESTIGATOR";
        FraudAlertResponse response = fraudService.updateAlertStatus(id, request, investigator);
        return ResponseEntity.ok(ApiResponse.ok(response, "Alert status updated successfully"));
    }

    @GetMapping("/alerts/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'FRAUD_INVESTIGATOR', 'AUDITOR')")
    @Operation(summary = "Get alert dashboard statistics", description = "Aggregates alert counts by status and severity tier")
    public ResponseEntity<ApiResponse<FraudAlertStatsResponse>> getAlertStats() {
        return ResponseEntity.ok(ApiResponse.ok(fraudService.getAlertStats(), "Fraud alert statistics retrieved"));
    }
}
