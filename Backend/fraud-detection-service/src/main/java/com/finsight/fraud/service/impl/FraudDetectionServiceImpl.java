package com.finsight.fraud.service.impl;

import com.finsight.common.context.CorrelationIdContext;
import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.fraud.client.AuditClient;
import com.finsight.fraud.client.FinancialDataClient;
import com.finsight.fraud.client.dto.TransactionSummaryDto;
import com.finsight.fraud.dto.request.EvaluateTransactionRequest;
import com.finsight.fraud.dto.request.UpdateAlertStatusRequest;
import com.finsight.fraud.dto.response.FraudAlertResponse;
import com.finsight.fraud.dto.response.FraudAlertStatsResponse;
import com.finsight.fraud.dto.response.FraudEvaluationResponse;
import com.finsight.fraud.entity.FraudAlert;
import com.finsight.fraud.enums.AlertStatus;
import com.finsight.fraud.enums.FraudRule;
import com.finsight.fraud.enums.RiskSeverity;
import com.finsight.fraud.mapper.FraudMapper;
import com.finsight.fraud.repository.FraudAlertRepository;
import com.finsight.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionServiceImpl.class);

    private final FraudAlertRepository alertRepository;
    private final FraudMapper mapper;
    private final FinancialDataClient financialDataClient;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public FraudEvaluationResponse evaluateTransaction(EvaluateTransactionRequest request) {
        log.info("[Fraud Detection] Evaluating transaction id={} ref={} amount={} accountId={}",
                request.getTransactionId(), request.getTransactionReference(), request.getAmount(), request.getAccountId());

        int score = 0;
        List<String> triggeredRules = new ArrayList<>();
        List<String> anomalyReasons = new ArrayList<>();

        // 1. High Amount Anomaly
        BigDecimal amount = request.getAmount();
        if (amount.compareTo(new BigDecimal("50000.00")) >= 0) {
            score += 40;
            triggeredRules.add(FraudRule.HIGH_AMOUNT_ANOMALY.name());
            anomalyReasons.add("Critical amount spike >= $50,000 threshold");
        } else if (amount.compareTo(new BigDecimal("10000.00")) >= 0) {
            score += 25;
            triggeredRules.add(FraudRule.HIGH_AMOUNT_ANOMALY.name());
            anomalyReasons.add("High transaction amount >= $10,000 threshold");
        }

        // 2. Suspicious Round Amount Check
        if (amount.compareTo(new BigDecimal("5000.00")) >= 0
                && amount.remainder(BigDecimal.valueOf(1000)).compareTo(BigDecimal.ZERO) == 0) {
            score += 15;
            triggeredRules.add(FraudRule.SUSPICIOUS_ROUND_AMOUNT.name());
            anomalyReasons.add("Exact round thousands amount structure");
        }

        // 3. Cross-Border / Currency Anomaly
        if (request.isInternational()) {
            score += 20;
            triggeredRules.add(FraudRule.CROSS_BORDER_ANOMALY.name());
            anomalyReasons.add("Cross-border foreign transaction origin");
        } else if (!"USD".equalsIgnoreCase(request.getCurrency()) && amount.compareTo(new BigDecimal("5000.00")) >= 0) {
            score += 15;
            triggeredRules.add(FraudRule.CROSS_BORDER_ANOMALY.name());
            anomalyReasons.add("High-value foreign currency transaction: " + request.getCurrency());
        }

        // 4. Connected Accounts / Self-loop
        if (request.getCounterpartAccount() != null && request.getAccountNumber() != null
                && request.getCounterpartAccount().equalsIgnoreCase(request.getAccountNumber())) {
            score += 35;
            triggeredRules.add(FraudRule.CONNECTED_ACCOUNTS_CYCLE.name());
            anomalyReasons.add("Self-transfer loop transaction into identical account number");
        }

        // 5. History Velocity & Duplicate Check via Financial Data Client
        try {
            ApiResponse<List<TransactionSummaryDto>> histResp = financialDataClient.getTransactionHistory(request.getAccountId());
            if (histResp != null && histResp.getData() != null) {
                List<TransactionSummaryDto> history = histResp.getData();

                LocalDate targetDate = request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now();
                long sameDateCount = history.stream()
                        .filter(t -> t.getTransactionDate() != null && t.getTransactionDate().isEqual(targetDate))
                        .count();

                if (sameDateCount >= 3) {
                    score += 25;
                    triggeredRules.add(FraudRule.HIGH_VELOCITY_SPIKE.name());
                    anomalyReasons.add("High velocity pattern: " + sameDateCount + " prior transactions on same date");
                }

                boolean hasDuplicate = history.stream()
                        .anyMatch(t -> t.getAmount() != null && t.getAmount().compareTo(amount) == 0
                                && !Objects.equals(t.getId(), request.getTransactionId()));

                if (hasDuplicate) {
                    score += 30;
                    triggeredRules.add(FraudRule.DUPLICATE_CANDIDATE.name());
                    anomalyReasons.add("Duplicate candidate: identical amount previously executed for this account");
                }
            }
        } catch (Exception e) {
            log.warn("[Fraud Detection] Could not fetch transaction history for account id={}: {}", request.getAccountId(), e.getMessage());
        }

        int finalScore = Math.min(100, Math.max(0, score));
        RiskSeverity severity = computeSeverity(finalScore);

        boolean alertGenerated = false;
        String alertRef = null;

        // Trigger alert if score >= 50
        if (finalScore >= 50) {
            alertGenerated = true;
            alertRef = "FRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String rulesStr = String.join(",", triggeredRules);
            String detailsStr = String.join("; ", anomalyReasons);

            FraudAlert alert = FraudAlert.builder()
                    .alertReference(alertRef)
                    .transactionId(request.getTransactionId())
                    .transactionReference(request.getTransactionReference())
                    .accountId(request.getAccountId())
                    .accountNumber(request.getAccountNumber())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .riskScore(finalScore)
                    .severity(severity)
                    .triggeredRules(rulesStr)
                    .details(detailsStr)
                    .status(AlertStatus.NEW)
                    .build();

            alert = alertRepository.save(alert);
            log.warn("[Fraud Detection] Generated ALERT {} (score={}, severity={}) for tx id={}",
                    alertRef, finalScore, severity, request.getTransactionId());

            // Audit event for fraud alert creation
            try {
                auditClient.recordAuditEvent(AuditEventDto.builder()
                        .actorId("FRAUD_ENGINE")
                        .actorType("SYSTEM")
                        .action("FRAUD_ALERT_GENERATED")
                        .entityType("FRAUD_ALERT")
                        .entityId(String.valueOf(alert.getId()))
                        .correlationId(CorrelationIdContext.get())
                        .outcome("ALERT_TRIGGERED")
                        .afterState(String.format("score=%d, severity=%s, rules=%s", finalScore, severity, rulesStr))
                        .metadata(Map.of(
                                "alertReference", alertRef,
                                "transactionId", request.getTransactionId(),
                                "riskScore", finalScore
                        ))
                        .build());
            } catch (Exception e) {
                log.warn("[Fraud Detection] Failed to send audit event: {}", e.getMessage());
            }
        }

        return FraudEvaluationResponse.builder()
                .transactionId(request.getTransactionId())
                .riskScore(finalScore)
                .severity(severity)
                .alertGenerated(alertGenerated)
                .alertReference(alertRef)
                .triggeredRules(triggeredRules)
                .details(String.join("; ", anomalyReasons))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<FraudAlertResponse> listAlerts(AlertStatus status, RiskSeverity severity, Pageable pageable) {
        Page<FraudAlert> page;
        if (status != null && severity != null) {
            page = alertRepository.findByStatusAndSeverity(status, severity, pageable);
        } else if (status != null) {
            page = alertRepository.findByStatus(status, pageable);
        } else if (severity != null) {
            page = alertRepository.findBySeverity(severity, pageable);
        } else {
            page = alertRepository.findAll(pageable);
        }

        List<FraudAlertResponse> content = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<FraudAlertResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FraudAlertResponse getAlertById(Long id) {
        FraudAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud alert not found with id: " + id));
        return mapper.toResponse(alert);
    }

    @Override
    @Transactional
    public FraudAlertResponse updateAlertStatus(Long id, UpdateAlertStatusRequest request, String investigator) {
        log.info("[Fraud Detection] Updating alert id={} status to {} by {}", id, request.getStatus(), investigator);

        FraudAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud alert not found with id: " + id));

        String beforeState = String.format("status=%s, assignedTo=%s, notes=%s",
                alert.getStatus(), alert.getAssignedTo(), alert.getResolutionNotes());

        alert.setStatus(request.getStatus());
        if (request.getAssignedTo() != null && !request.getAssignedTo().isBlank()) {
            alert.setAssignedTo(request.getAssignedTo());
        } else if (alert.getAssignedTo() == null) {
            alert.setAssignedTo(investigator);
        }

        if (request.getResolutionNotes() != null) {
            alert.setResolutionNotes(request.getResolutionNotes());
        }

        if (request.getStatus() == AlertStatus.CONFIRMED_FRAUD ||
                request.getStatus() == AlertStatus.FALSE_POSITIVE ||
                request.getStatus() == AlertStatus.DISMISSED) {
            alert.setResolvedAt(Instant.now());
        }

        FraudAlert updated = alertRepository.save(alert);
        String afterState = String.format("status=%s, assignedTo=%s, notes=%s",
                updated.getStatus(), updated.getAssignedTo(), updated.getResolutionNotes());

        try {
            auditClient.recordAuditEvent(AuditEventDto.builder()
                    .actorId(investigator)
                    .actorType("USER")
                    .action("FRAUD_ALERT_STATUS_CHANGE")
                    .entityType("FRAUD_ALERT")
                    .entityId(String.valueOf(id))
                    .correlationId(CorrelationIdContext.get())
                    .outcome("STATUS_UPDATED")
                    .beforeState(beforeState)
                    .afterState(afterState)
                    .metadata(Map.of("newStatus", request.getStatus().name()))
                    .build());
        } catch (Exception e) {
            log.warn("[Fraud Detection] Failed to record audit event for alert status change: {}", e.getMessage());
        }

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public FraudAlertStatsResponse getAlertStats() {
        long total = alertRepository.count();
        long newCount = alertRepository.countByStatus(AlertStatus.NEW);
        long underReview = alertRepository.countByStatus(AlertStatus.UNDER_REVIEW);
        long confirmed = alertRepository.countByStatus(AlertStatus.CONFIRMED_FRAUD);
        long falsePositive = alertRepository.countByStatus(AlertStatus.FALSE_POSITIVE);
        long dismissed = alertRepository.countByStatus(AlertStatus.DISMISSED);

        long critical = alertRepository.countBySeverity(RiskSeverity.CRITICAL);
        long high = alertRepository.countBySeverity(RiskSeverity.HIGH);
        long medium = alertRepository.countBySeverity(RiskSeverity.MEDIUM);
        long low = alertRepository.countBySeverity(RiskSeverity.LOW);

        return FraudAlertStatsResponse.builder()
                .totalAlerts(total)
                .newAlerts(newCount)
                .underReviewAlerts(underReview)
                .confirmedFraudAlerts(confirmed)
                .falsePositiveAlerts(falsePositive)
                .dismissedAlerts(dismissed)
                .criticalAlerts(critical)
                .highAlerts(high)
                .mediumAlerts(medium)
                .lowAlerts(low)
                .build();
    }

    private RiskSeverity computeSeverity(int score) {
        if (score >= 90) return RiskSeverity.CRITICAL;
        if (score >= 70) return RiskSeverity.HIGH;
        if (score >= 30) return RiskSeverity.MEDIUM;
        return RiskSeverity.LOW;
    }
}
