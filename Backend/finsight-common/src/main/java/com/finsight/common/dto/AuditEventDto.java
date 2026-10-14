package com.finsight.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventDto {
    private String actorId;
    private String actorType;     // USER, SYSTEM, AGENT, GATEWAY
    private String action;        // USER_LOGIN, RECORD_IMPORT, RECONCILIATION_RUN, FRAUD_ALERT_STATUS_CHANGE, AGENT_EXECUTION, etc.
    private String entityType;    // USER, TRANSACTION, RECON_CASE, FRAUD_ALERT, AGENT_RUN, REPORT
    private String entityId;
    private String correlationId;
    private String outcome;       // SUCCESS, FAILURE, REJECTED, APPROVED
    private String beforeState;   // JSON snapshot or summary
    private String afterState;    // JSON snapshot or summary
    private Map<String, Object> metadata;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
