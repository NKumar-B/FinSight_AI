package com.finsight.fraud.dto.response;

import com.finsight.fraud.enums.RiskSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudEvaluationResponse {
    private Long transactionId;
    private int riskScore;
    private RiskSeverity severity;
    private boolean alertGenerated;
    private String alertReference;
    private List<String> triggeredRules;
    private String details;
}
