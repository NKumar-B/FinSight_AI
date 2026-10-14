package com.finsight.audit.controller;

import com.finsight.audit.dto.response.AuditEventResponse;
import com.finsight.audit.dto.response.AuditSummaryStatsResponse;
import com.finsight.audit.service.AuditService;
import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/audit/events")
@RequiredArgsConstructor
@Tag(name = "Audit Events", description = "Endpoints for immutable event ingestion, querying, and audit trail retrieval")
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    @Operation(summary = "Record audit event", description = "Ingests an audit event from any microservice or actor")
    public ResponseEntity<ApiResponse<AuditEventResponse>> recordEvent(@RequestBody AuditEventDto dto) {
        AuditEventResponse response = auditService.recordEvent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Audit event recorded"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Query audit events", description = "Search and filter immutable audit trails by actor, action, entity, correlation ID, or date range")
    public ResponseEntity<ApiResponse<PagedResponse<AuditEventResponse>>> getEvents(
            @RequestParam(name = "actorId", required = false) String actorId,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "entityType", required = false) String entityType,
            @RequestParam(name = "correlationId", required = false) String correlationId,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                auditService.getEvents(actorId, action, entityType, correlationId, from, to, pageable),
                "Audit events retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Get audit event by ID", description = "Retrieve complete before and after state snapshots of a specific audit event")
    public ResponseEntity<ApiResponse<AuditEventResponse>> getEventById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(auditService.getEventById(id), "Audit event details retrieved"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Get audit statistics", description = "Retrieve aggregated event statistics, actor breakdowns, and top actions")
    public ResponseEntity<ApiResponse<AuditSummaryStatsResponse>> getAuditStats() {
        return ResponseEntity.ok(ApiResponse.ok(auditService.getAuditStats(), "Audit statistics retrieved"));
    }
}
