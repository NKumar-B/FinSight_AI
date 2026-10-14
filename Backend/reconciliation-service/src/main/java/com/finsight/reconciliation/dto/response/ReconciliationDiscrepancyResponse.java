package com.finsight.reconciliation.dto.response;

import com.finsight.reconciliation.enums.CaseStatus;
import com.finsight.reconciliation.enums.DiscrepancyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationDiscrepancyResponse {
    private Long id;
    private Long runId;
    private String caseReference;
    private DiscrepancyType discrepancyType;
    private Long sourceTransactionId;
    private String sourceTransactionRef;
    private Long targetTransactionId;
    private String targetTransactionRef;
    private BigDecimal sourceAmount;
    private BigDecimal targetAmount;
    private BigDecimal amountDiscrepancy;
    private LocalDate sourceDate;
    private LocalDate targetDate;
    private CaseStatus status;
    private String assignedTo;
    private String resolutionNotes;
    private Instant createdAt;
    private Instant resolvedAt;
}
