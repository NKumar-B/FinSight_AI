package com.finsight.reconciliation.service.impl;

import com.finsight.common.context.CorrelationIdContext;
import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.reconciliation.client.AuditClient;
import com.finsight.reconciliation.client.FinancialDataClient;
import com.finsight.reconciliation.client.dto.ExternalTransactionDto;
import com.finsight.reconciliation.dto.request.ExecuteReconciliationRequest;
import com.finsight.reconciliation.dto.response.ReconciliationDiscrepancyResponse;
import com.finsight.reconciliation.dto.response.ReconciliationMatchResponse;
import com.finsight.reconciliation.dto.response.ReconciliationRunResponse;
import com.finsight.reconciliation.entity.ReconciliationDiscrepancy;
import com.finsight.reconciliation.entity.ReconciliationMatch;
import com.finsight.reconciliation.entity.ReconciliationRun;
import com.finsight.reconciliation.enums.*;
import com.finsight.reconciliation.mapper.ReconciliationMapper;
import com.finsight.reconciliation.repository.ReconciliationDiscrepancyRepository;
import com.finsight.reconciliation.repository.ReconciliationMatchRepository;
import com.finsight.reconciliation.repository.ReconciliationRunRepository;
import com.finsight.reconciliation.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReconciliationServiceImpl implements ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationServiceImpl.class);

    private final ReconciliationRunRepository runRepository;
    private final ReconciliationMatchRepository matchRepository;
    private final ReconciliationDiscrepancyRepository discrepancyRepository;
    private final ReconciliationMapper mapper;
    private final FinancialDataClient financialDataClient;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public ReconciliationRunResponse executeReconciliation(ExecuteReconciliationRequest request, String executedBy) {
        log.info("[Reconciliation] Starting reconciliation execution by {}", executedBy);

        int dateToleranceDays = request.getDateToleranceDays() > 0 ? request.getDateToleranceDays() : 2;
        BigDecimal amountTolerance = request.getAmountTolerance() != null ? request.getAmountTolerance() : new BigDecimal("0.05");

        // 1. Fetch internal unreconciled transactions
        List<ExternalTransactionDto> sourceTransactions = Collections.emptyList();
        try {
            ApiResponse<List<ExternalTransactionDto>> sourceResp = financialDataClient.getUnreconciledTransactions();
            if (sourceResp != null && sourceResp.getData() != null) {
                sourceTransactions = sourceResp.getData();
            }
        } catch (Exception e) {
            log.error("[Reconciliation] Failed to fetch source transactions from financial-data-service: {}", e.getMessage());
        }

        List<ExternalTransactionDto> targetTransactions = request.getExternalCounterpartTransactions() != null
                ? request.getExternalCounterpartTransactions()
                : Collections.emptyList();

        String runRef = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ReconciliationRun run = ReconciliationRun.builder()
                .runReference(runRef)
                .status(ReconciliationRunStatus.IN_PROGRESS)
                .totalSourceRecords(sourceTransactions.size())
                .totalTargetRecords(targetTransactions.size())
                .dateToleranceDays(dateToleranceDays)
                .amountTolerance(amountTolerance)
                .executedBy(executedBy)
                .build();
        run = runRepository.save(run);

        Set<Long> matchedSourceIds = new HashSet<>();
        Set<Long> matchedTargetIndices = new HashSet<>();
        List<ReconciliationMatch> matches = new ArrayList<>();
        List<ReconciliationDiscrepancy> discrepancies = new ArrayList<>();

        // 2. Exact Matching
        for (ExternalTransactionDto src : sourceTransactions) {
            if (matchedSourceIds.contains(src.getId())) continue;

            for (int i = 0; i < targetTransactions.size(); i++) {
                if (matchedTargetIndices.contains((long) i)) continue;
                ExternalTransactionDto tgt = targetTransactions.get(i);

                boolean refMatches = referencesMatch(src.getTransactionReference(), tgt.getTransactionReference());
                boolean amountMatches = amountsMatch(src.getAmount(), tgt.getAmount(), BigDecimal.ZERO);
                boolean dateMatches = datesMatch(src.getTransactionDate(), tgt.getTransactionDate(), 0);

                if (refMatches && amountMatches && dateMatches) {
                    matchedSourceIds.add(src.getId());
                    matchedTargetIndices.add((long) i);

                    matches.add(ReconciliationMatch.builder()
                            .reconciliationRun(run)
                            .sourceTransactionId(src.getId())
                            .sourceTransactionRef(src.getTransactionReference())
                            .targetTransactionId(tgt.getId())
                            .targetTransactionRef(tgt.getTransactionReference())
                            .matchType(MatchType.EXACT)
                            .amountDifference(BigDecimal.ZERO)
                            .dateDifferenceDays(0)
                            .status(MatchStatus.AUTOMATICALLY_MATCHED)
                            .build());
                    break;
                }
            }
        }

        // 3. Tolerance Matching
        for (ExternalTransactionDto src : sourceTransactions) {
            if (matchedSourceIds.contains(src.getId())) continue;

            for (int i = 0; i < targetTransactions.size(); i++) {
                if (matchedTargetIndices.contains((long) i)) continue;
                ExternalTransactionDto tgt = targetTransactions.get(i);

                boolean refMatches = referencesMatch(src.getTransactionReference(), tgt.getTransactionReference());
                BigDecimal amtDiff = calculateAmountDiff(src.getAmount(), tgt.getAmount());
                int dayDiff = calculateDateDiff(src.getTransactionDate(), tgt.getTransactionDate());

                if (refMatches && amtDiff.compareTo(amountTolerance) <= 0 && dayDiff <= dateToleranceDays) {
                    matchedSourceIds.add(src.getId());
                    matchedTargetIndices.add((long) i);

                    matches.add(ReconciliationMatch.builder()
                            .reconciliationRun(run)
                            .sourceTransactionId(src.getId())
                            .sourceTransactionRef(src.getTransactionReference())
                            .targetTransactionId(tgt.getId())
                            .targetTransactionRef(tgt.getTransactionReference())
                            .matchType(MatchType.TOLERANCE)
                            .amountDifference(amtDiff)
                            .dateDifferenceDays(dayDiff)
                            .status(MatchStatus.AUTOMATICALLY_MATCHED)
                            .build());
                    break;
                }
            }
        }

        // 4. One-to-Many Matching (1 Source transaction matching sum of 2+ Targets)
        for (ExternalTransactionDto src : sourceTransactions) {
            if (matchedSourceIds.contains(src.getId())) continue;

            List<Integer> candidateTargetIndices = new ArrayList<>();
            BigDecimal sum = BigDecimal.ZERO;

            for (int i = 0; i < targetTransactions.size(); i++) {
                if (matchedTargetIndices.contains((long) i)) continue;
                ExternalTransactionDto tgt = targetTransactions.get(i);

                if (referencesMatch(src.getTransactionReference(), tgt.getTransactionReference()) ||
                        (src.getCounterpartAccount() != null && src.getCounterpartAccount().equalsIgnoreCase(tgt.getAccountNumber()))) {
                    sum = sum.add(tgt.getAmount() != null ? tgt.getAmount() : BigDecimal.ZERO);
                    candidateTargetIndices.add(i);

                    BigDecimal diff = src.getAmount().subtract(sum).abs();
                    if (candidateTargetIndices.size() >= 2 && diff.compareTo(amountTolerance) <= 0) {
                        matchedSourceIds.add(src.getId());
                        for (Integer idx : candidateTargetIndices) {
                            matchedTargetIndices.add(idx.longValue());
                        }

                        String targetIds = candidateTargetIndices.stream()
                                .map(idx -> String.valueOf(targetTransactions.get(idx).getId()))
                                .collect(Collectors.joining(","));

                        matches.add(ReconciliationMatch.builder()
                                .reconciliationRun(run)
                                .sourceTransactionId(src.getId())
                                .sourceTransactionRef(src.getTransactionReference())
                                .targetTransactionIds(targetIds)
                                .matchType(MatchType.ONE_TO_MANY)
                                .amountDifference(diff)
                                .dateDifferenceDays(0)
                                .status(MatchStatus.AUTOMATICALLY_MATCHED)
                                .build());
                        break;
                    }
                }
            }
        }

        // 5. Discrepancy Detection for remaining unmatched records
        Set<Long> discrepancySourceIds = new HashSet<>();
        Set<Long> discrepancyTargetIndices = new HashSet<>();

        // 5a. Amount Mismatch & Timing Difference for remaining matching references
        for (ExternalTransactionDto src : sourceTransactions) {
            if (matchedSourceIds.contains(src.getId()) || discrepancySourceIds.contains(src.getId())) continue;

            for (int i = 0; i < targetTransactions.size(); i++) {
                if (matchedTargetIndices.contains((long) i) || discrepancyTargetIndices.contains((long) i)) continue;
                ExternalTransactionDto tgt = targetTransactions.get(i);

                if (referencesMatch(src.getTransactionReference(), tgt.getTransactionReference())) {
                    BigDecimal amtDiff = calculateAmountDiff(src.getAmount(), tgt.getAmount());
                    int dayDiff = calculateDateDiff(src.getTransactionDate(), tgt.getTransactionDate());

                    DiscrepancyType discType;
                    if (amtDiff.compareTo(amountTolerance) > 0) {
                        discType = DiscrepancyType.AMOUNT_MISMATCH;
                    } else if (dayDiff > dateToleranceDays) {
                        discType = DiscrepancyType.TIMING_DIFFERENCE;
                    } else {
                        discType = DiscrepancyType.MULTI_TRANSACTION_MISMATCH;
                    }

                    discrepancies.add(ReconciliationDiscrepancy.builder()
                            .reconciliationRun(run)
                            .caseReference("DISC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                            .discrepancyType(discType)
                            .sourceTransactionId(src.getId())
                            .sourceTransactionRef(src.getTransactionReference())
                            .targetTransactionId(tgt.getId())
                            .targetTransactionRef(tgt.getTransactionReference())
                            .sourceAmount(src.getAmount())
                            .targetAmount(tgt.getAmount())
                            .amountDiscrepancy(amtDiff)
                            .sourceDate(src.getTransactionDate())
                            .targetDate(tgt.getTransactionDate())
                            .status(CaseStatus.OPEN)
                            .build());

                    discrepancySourceIds.add(src.getId());
                    discrepancyTargetIndices.add((long) i);
                    break;
                }
            }
        }

        // 5b. Unmatched Source Records
        for (ExternalTransactionDto src : sourceTransactions) {
            if (!matchedSourceIds.contains(src.getId()) && !discrepancySourceIds.contains(src.getId())) {
                discrepancies.add(ReconciliationDiscrepancy.builder()
                        .reconciliationRun(run)
                        .caseReference("DISC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .discrepancyType(DiscrepancyType.UNMATCHED_SOURCE)
                        .sourceTransactionId(src.getId())
                        .sourceTransactionRef(src.getTransactionReference())
                        .sourceAmount(src.getAmount())
                        .sourceDate(src.getTransactionDate())
                        .status(CaseStatus.OPEN)
                        .build());
            }
        }

        // 5c. Unmatched Target Records
        for (int i = 0; i < targetTransactions.size(); i++) {
            if (!matchedTargetIndices.contains((long) i) && !discrepancyTargetIndices.contains((long) i)) {
                ExternalTransactionDto tgt = targetTransactions.get(i);
                discrepancies.add(ReconciliationDiscrepancy.builder()
                        .reconciliationRun(run)
                        .caseReference("DISC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .discrepancyType(DiscrepancyType.UNMATCHED_TARGET)
                        .targetTransactionId(tgt.getId())
                        .targetTransactionRef(tgt.getTransactionReference())
                        .targetAmount(tgt.getAmount())
                        .targetDate(tgt.getTransactionDate())
                        .status(CaseStatus.OPEN)
                        .build());
            }
        }

        // Save matches and discrepancies
        matchRepository.saveAll(matches);
        discrepancyRepository.saveAll(discrepancies);

        // Update run statistics
        int exactCount = (int) matches.stream().filter(m -> m.getMatchType() == MatchType.EXACT).count();
        int toleranceCount = (int) matches.stream().filter(m -> m.getMatchType() == MatchType.TOLERANCE || m.getMatchType() == MatchType.ONE_TO_MANY).count();
        int unmatchedTotal = discrepancies.size();

        run.setExactMatchesCount(exactCount);
        run.setToleranceMatchesCount(toleranceCount);
        run.setDiscrepanciesCount(discrepancies.size());
        run.setUnmatchedCount(unmatchedTotal);
        run.setStatus(ReconciliationRunStatus.COMPLETED);
        run.setCompletedAt(Instant.now());
        ReconciliationRun savedRun = runRepository.save(run);

        // 6. Notify Financial Data Service to mark matched source transactions as RECONCILED
        for (Long sourceId : matchedSourceIds) {
            try {
                financialDataClient.updateTransactionStatus(sourceId, Map.of("status", "RECONCILED"));
            } catch (Exception e) {
                log.warn("[Reconciliation] Could not update status for transaction id={}: {}", sourceId, e.getMessage());
            }
        }

        // 7. Record Audit Event
        try {
            auditClient.recordAuditEvent(AuditEventDto.builder()
                    .actorId(executedBy)
                    .actorType("USER")
                    .action("RECONCILIATION_RUN")
                    .entityType("RECON_RUN")
                    .entityId(String.valueOf(savedRun.getId()))
                    .correlationId(CorrelationIdContext.get())
                    .outcome("SUCCESS")
                    .afterState(String.format("Exact: %d, Tolerance: %d, Discrepancies: %d", exactCount, toleranceCount, discrepancies.size()))
                    .metadata(Map.of(
                            "runReference", runRef,
                            "totalSourceRecords", sourceTransactions.size(),
                            "totalTargetRecords", targetTransactions.size()
                    ))
                    .build());
        } catch (Exception e) {
            log.warn("[Reconciliation] Could not publish audit event: {}", e.getMessage());
        }

        log.info("[Reconciliation] Completed run {}. Matches={}, Discrepancies={}", runRef, matches.size(), discrepancies.size());
        return mapper.toRunResponse(savedRun);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReconciliationRunResponse> listRuns(Pageable pageable) {
        Page<ReconciliationRun> page = runRepository.findAll(pageable);
        List<ReconciliationRunResponse> content = page.getContent().stream()
                .map(mapper::toRunResponse)
                .collect(Collectors.toList());
        return PagedResponse.<ReconciliationRunResponse>builder()
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
    public ReconciliationRunResponse getRunById(Long id) {
        ReconciliationRun run = runRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation run not found with id: " + id));
        return mapper.toRunResponse(run);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReconciliationMatchResponse> getMatchesByRun(Long runId, Pageable pageable) {
        if (!runRepository.existsById(runId)) {
            throw new ResourceNotFoundException("Reconciliation run not found with id: " + runId);
        }
        Page<ReconciliationMatch> page = matchRepository.findByReconciliationRunId(runId, pageable);
        List<ReconciliationMatchResponse> content = page.getContent().stream()
                .map(mapper::toMatchResponse)
                .collect(Collectors.toList());
        return PagedResponse.<ReconciliationMatchResponse>builder()
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
    public PagedResponse<ReconciliationDiscrepancyResponse> getDiscrepanciesByRun(Long runId, Pageable pageable) {
        if (!runRepository.existsById(runId)) {
            throw new ResourceNotFoundException("Reconciliation run not found with id: " + runId);
        }
        Page<ReconciliationDiscrepancy> page = discrepancyRepository.findByReconciliationRunId(runId, pageable);
        List<ReconciliationDiscrepancyResponse> content = page.getContent().stream()
                .map(mapper::toDiscrepancyResponse)
                .collect(Collectors.toList());
        return PagedResponse.<ReconciliationDiscrepancyResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private boolean referencesMatch(String ref1, String ref2) {
        if (ref1 == null || ref2 == null) return false;
        return ref1.trim().equalsIgnoreCase(ref2.trim());
    }

    private boolean amountsMatch(BigDecimal amt1, BigDecimal amt2, BigDecimal tolerance) {
        if (amt1 == null || amt2 == null) return false;
        return amt1.subtract(amt2).abs().compareTo(tolerance) <= 0;
    }

    private boolean datesMatch(java.time.LocalDate d1, java.time.LocalDate d2, int maxDays) {
        if (d1 == null || d2 == null) return false;
        return Math.abs(ChronoUnit.DAYS.between(d1, d2)) <= maxDays;
    }

    private BigDecimal calculateAmountDiff(BigDecimal a1, BigDecimal a2) {
        if (a1 == null && a2 == null) return BigDecimal.ZERO;
        if (a1 == null) return a2;
        if (a2 == null) return a1;
        return a1.subtract(a2).abs();
    }

    private int calculateDateDiff(java.time.LocalDate d1, java.time.LocalDate d2) {
        if (d1 == null || d2 == null) return 0;
        return (int) Math.abs(ChronoUnit.DAYS.between(d1, d2));
    }
}
