package com.finsight.audit.mapper;

import com.finsight.audit.dto.response.AuditEventResponse;
import com.finsight.audit.dto.response.ComplianceReportResponse;
import com.finsight.audit.entity.AuditEvent;
import com.finsight.audit.entity.ComplianceReport;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    public AuditEventResponse toEventResponse(AuditEvent event) {
        if (event == null) return null;
        return AuditEventResponse.builder()
                .id(event.getId())
                .actorId(event.getActorId())
                .actorType(event.getActorType())
                .action(event.getAction())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .correlationId(event.getCorrelationId())
                .outcome(event.getOutcome())
                .beforeState(event.getBeforeState())
                .afterState(event.getAfterState())
                .metadata(event.getMetadata())
                .timestamp(event.getTimestamp())
                .build();
    }

    public ComplianceReportResponse toReportResponse(ComplianceReport report) {
        if (report == null) return null;
        return ComplianceReportResponse.builder()
                .id(report.getId())
                .reportReference(report.getReportReference())
                .reportType(report.getReportType())
                .generatedBy(report.getGeneratedBy())
                .totalEventsCount(report.getTotalEventsCount())
                .summaryContent(report.getSummaryContent())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
