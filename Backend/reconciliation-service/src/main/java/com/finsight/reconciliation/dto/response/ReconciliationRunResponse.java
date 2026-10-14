package com.finsight.reconciliation.dto.response;

import com.finsight.reconciliation.enums.ReconciliationRunStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationRunResponse {
    private Long id;
    private String runReference;
    private ReconciliationRunStatus status;
    private int totalSourceRecords;
    private int totalTargetRecords;
    private int exactMatchesCount;
    private int toleranceMatchesCount;
    private int discrepanciesCount;
    private int unmatchedCount;
    private int dateToleranceDays;
    private BigDecimal amountTolerance;
    private String executedBy;
    private Instant createdAt;
    private Instant completedAt;
}
