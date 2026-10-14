package com.finsight.audit.service;

import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import com.finsight.audit.dto.response.AuditEventResponse;
import com.finsight.audit.dto.response.AuditSummaryStatsResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AuditService {
    AuditEventResponse recordEvent(AuditEventDto dto);
    PagedResponse<AuditEventResponse> getEvents(String actorId, String action, String entityType, String correlationId, Instant from, Instant to, Pageable pageable);
    AuditEventResponse getEventById(Long id);
    AuditSummaryStatsResponse getAuditStats();
}
