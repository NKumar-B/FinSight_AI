package com.finsight.audit.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateReportRequest {

    @NotBlank(message = "Report type is required")
    private String reportType;

    private Instant startDate;

    private Instant endDate;

    private String entityType;

    private String actorId;
}
