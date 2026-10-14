package com.finsight.reconciliation.dto.response;

import com.finsight.reconciliation.enums.DecisionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDecisionResponse {
    private Long id;
    private Long caseId;
    private Long matchId;
    private DecisionType decision;
    private String reviewerId;
    private String reason;
    private String beforeState;
    private String afterState;
    private Instant createdAt;
}
