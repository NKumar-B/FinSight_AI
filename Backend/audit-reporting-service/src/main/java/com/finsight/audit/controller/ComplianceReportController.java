package com.finsight.audit.controller;

import com.finsight.audit.dto.request.GenerateReportRequest;
import com.finsight.audit.dto.response.ComplianceReportResponse;
import com.finsight.audit.service.ComplianceReportService;
import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
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
@RequestMapping("/api/v1/audit/reports")
@RequiredArgsConstructor
@Tag(name = "Compliance Reports", description = "Endpoints for generating and viewing compliance and audit reports")
public class ComplianceReportController {

    private final ComplianceReportService reportService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Generate compliance report", description = "Aggregates events over a criteria window and generates structured compliance document")
    public ResponseEntity<ApiResponse<ComplianceReportResponse>> generateReport(
            @Valid @RequestBody GenerateReportRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "AUDIT_OFFICER";
        ComplianceReportResponse response = reportService.generateReport(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Compliance report generated successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "List compliance reports", description = "Returns paginated list of generated compliance reports")
    public ResponseEntity<ApiResponse<PagedResponse<ComplianceReportResponse>>> listReports(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.listReports(pageable), "Compliance reports retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Get compliance report by ID", description = "Retrieve full summary and content of a compliance report")
    public ResponseEntity<ApiResponse<ComplianceReportResponse>> getReportById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getReportById(id), "Compliance report retrieved"));
    }
}
