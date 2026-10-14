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
public class ComplianceReportResponse {
    private Long id;
    private String reportReference;
    private String reportType;
    private String generatedBy;
    private int totalEventsCount;
    private String summaryContent;
    private Instant createdAt;
}
