package com.finsight.reconciliation.dto.response;

import com.finsight.reconciliation.enums.MatchStatus;
import com.finsight.reconciliation.enums.MatchType;
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
public class ReconciliationMatchResponse {
    private Long id;
    private Long runId;
    private Long sourceTransactionId;
    private String sourceTransactionRef;
    private Long targetTransactionId;
    private String targetTransactionRef;
    private String targetTransactionIds;
    private MatchType matchType;
    private BigDecimal amountDifference;
    private int dateDifferenceDays;
    private MatchStatus status;
    private String reviewedBy;
    private String reviewNotes;
    private Instant createdAt;
}
