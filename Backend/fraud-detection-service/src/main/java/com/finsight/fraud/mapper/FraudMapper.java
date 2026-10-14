package com.finsight.fraud.mapper;

import com.finsight.fraud.dto.response.FraudAlertResponse;
import com.finsight.fraud.entity.FraudAlert;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class FraudMapper {

    public FraudAlertResponse toResponse(FraudAlert alert) {
        if (alert == null) return null;

        List<String> rulesList = Collections.emptyList();
        if (alert.getTriggeredRules() != null && !alert.getTriggeredRules().isBlank()) {
            rulesList = Arrays.stream(alert.getTriggeredRules().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        return FraudAlertResponse.builder()
                .id(alert.getId())
                .alertReference(alert.getAlertReference())
                .transactionId(alert.getTransactionId())
                .transactionReference(alert.getTransactionReference())
                .accountId(alert.getAccountId())
                .accountNumber(alert.getAccountNumber())
                .amount(alert.getAmount())
                .currency(alert.getCurrency())
                .riskScore(alert.getRiskScore())
                .severity(alert.getSeverity())
                .triggeredRules(rulesList)
                .details(alert.getDetails())
                .status(alert.getStatus())
                .assignedTo(alert.getAssignedTo())
                .resolutionNotes(alert.getResolutionNotes())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .resolvedAt(alert.getResolvedAt())
                .build();
    }
}
