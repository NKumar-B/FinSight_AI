package com.finsight.fraud.dto.response;

import com.finsight.fraud.enums.AlertStatus;
import com.finsight.fraud.enums.RiskSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlertResponse {
    private Long id;
    private String alertReference;
    private Long transactionId;
    private String transactionReference;
    private Long accountId;
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private int riskScore;
    private RiskSeverity severity;
    private List<String> triggeredRules;
    private String details;
    private AlertStatus status;
    private String assignedTo;
    private String resolutionNotes;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
}
