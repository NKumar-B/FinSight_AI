package com.finsight.reconciliation.service.impl;

import com.finsight.common.context.CorrelationIdContext;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.reconciliation.client.AuditClient;
import com.finsight.reconciliation.dto.request.ReviewDecisionRequest;
import com.finsight.reconciliation.dto.response.ReconciliationDiscrepancyResponse;
import com.finsight.reconciliation.dto.response.ReviewDecisionResponse;
import com.finsight.reconciliation.entity.ReconciliationDiscrepancy;
import com.finsight.reconciliation.entity.ReviewDecision;
import com.finsight.reconciliation.enums.CaseStatus;
import com.finsight.reconciliation.enums.DecisionType;
import com.finsight.reconciliation.mapper.ReconciliationMapper;
import com.finsight.reconciliation.repository.ReconciliationDiscrepancyRepository;
import com.finsight.reconciliation.repository.ReviewDecisionRepository;
import com.finsight.reconciliation.service.ReconciliationReviewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReconciliationReviewServiceImpl implements ReconciliationReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationReviewServiceImpl.class);

    private final ReconciliationDiscrepancyRepository discrepancyRepository;
    private final ReviewDecisionRepository decisionRepository;
    private final ReconciliationMapper mapper;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public ReviewDecisionResponse submitDecision(Long caseId, ReviewDecisionRequest request, String reviewerId) {
        log.info("[Reconciliation Review] Submitting decision {} for case id={} by reviewer={}",
                request.getDecision(), caseId, reviewerId);

        ReconciliationDiscrepancy discrepancy = discrepancyRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation discrepancy not found with id: " + caseId));

        String beforeState = String.format("status=%s, assignedTo=%s, notes=%s",
                discrepancy.getStatus(), discrepancy.getAssignedTo(), discrepancy.getResolutionNotes());

        DecisionType dt = request.getDecision();
        if (dt == DecisionType.APPROVE_MATCH) {
            discrepancy.setStatus(CaseStatus.RESOLVED_APPROVED);
            discrepancy.setResolvedAt(Instant.now());
        } else if (dt == DecisionType.REJECT_MATCH) {
            discrepancy.setStatus(CaseStatus.RESOLVED_REJECTED);
            discrepancy.setResolvedAt(Instant.now());
        } else if (dt == DecisionType.WRITE_OFF) {
            discrepancy.setStatus(CaseStatus.RESOLVED_APPROVED);
            discrepancy.setResolvedAt(Instant.now());
        } else if (dt == DecisionType.ADJUSTMENT_REQUIRED) {
            discrepancy.setStatus(CaseStatus.UNDER_INVESTIGATION);
        } else if (dt == DecisionType.ESCALATE_TO_COMPLIANCE) {
            discrepancy.setStatus(CaseStatus.ESCALATED);
        }

        discrepancy.setAssignedTo(reviewerId);
        discrepancy.setResolutionNotes(request.getReason());
        ReconciliationDiscrepancy saved = discrepancyRepository.save(discrepancy);

        String afterState = String.format("status=%s, assignedTo=%s, notes=%s",
                saved.getStatus(), saved.getAssignedTo(), saved.getResolutionNotes());

        ReviewDecision decision = ReviewDecision.builder()
                .caseId(caseId)
                .decision(request.getDecision())
                .reviewerId(reviewerId)
                .reason(request.getReason())
                .beforeState(beforeState)
                .afterState(afterState)
                .build();
        decision = decisionRepository.save(decision);

        // Record Audit Event
        try {
            auditClient.recordAuditEvent(AuditEventDto.builder()
                    .actorId(reviewerId)
                    .actorType("USER")
                    .action("REVIEW_DECISION")
                    .entityType("RECON_CASE")
                    .entityId(String.valueOf(caseId))
                    .correlationId(CorrelationIdContext.get())
                    .outcome("SUCCESS")
                    .beforeState(beforeState)
                    .afterState(afterState)
                    .metadata(Map.of(
                            "decision", request.getDecision().name(),
                            "caseReference", discrepancy.getCaseReference()
                    ))
                    .build());
        } catch (Exception e) {
            log.warn("[Reconciliation Review] Could not log audit event: {}", e.getMessage());
        }

        return mapper.toDecisionResponse(decision);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReconciliationDiscrepancyResponse> getDiscrepancies(CaseStatus status, Pageable pageable) {
        Page<ReconciliationDiscrepancy> page;
        if (status != null) {
            page = discrepancyRepository.findByStatus(status, pageable);
        } else {
            page = discrepancyRepository.findAll(pageable);
        }

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

    @Override
    @Transactional(readOnly = true)
    public ReconciliationDiscrepancyResponse getDiscrepancyById(Long caseId) {
        ReconciliationDiscrepancy discrepancy = discrepancyRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation discrepancy not found with id: " + caseId));
        return mapper.toDiscrepancyResponse(discrepancy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDecisionResponse> getDecisionsByCaseId(Long caseId) {
        return decisionRepository.findByCaseId(caseId).stream()
                .map(mapper::toDecisionResponse)
                .collect(Collectors.toList());
    }
}
