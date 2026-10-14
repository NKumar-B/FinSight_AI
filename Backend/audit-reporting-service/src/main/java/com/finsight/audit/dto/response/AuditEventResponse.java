package com.finsight.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {
    private Long id;
    private String actorId;
    private String actorType;
    private String action;
    private String entityType;
    private String entityId;
    private String correlationId;
    private String outcome;
    private String beforeState;
    private String afterState;
    private String metadata;
    private Instant timestamp;
}
