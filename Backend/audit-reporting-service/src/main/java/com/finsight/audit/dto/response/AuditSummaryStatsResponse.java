package com.finsight.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditSummaryStatsResponse {
    private long totalEvents;
    private long successCount;
    private long failureCount;
    private Map<String, Long> eventsByActorType;
    private Map<String, Long> topActions;
}
