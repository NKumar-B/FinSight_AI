package com.finsight.audit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.audit.dto.response.AuditEventResponse;
import com.finsight.audit.dto.response.AuditSummaryStatsResponse;
import com.finsight.audit.entity.AuditEvent;
import com.finsight.audit.mapper.AuditMapper;
import com.finsight.audit.repository.AuditEventRepository;
import com.finsight.audit.service.AuditService;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditEventRepository auditRepository;
    private final AuditMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AuditEventResponse recordEvent(AuditEventDto dto) {
        String metadataJson = null;
        if (dto.getMetadata() != null && !dto.getMetadata().isEmpty()) {
            try {
                metadataJson = objectMapper.writeValueAsString(dto.getMetadata());
            } catch (Exception e) {
                log.warn("[Audit] Failed to serialize metadata: {}", e.getMessage());
            }
        }

        AuditEvent event = AuditEvent.builder()
                .actorId(dto.getActorId() != null ? dto.getActorId() : "ANONYMOUS")
                .actorType(dto.getActorType() != null ? dto.getActorType() : "SYSTEM")
                .action(dto.getAction() != null ? dto.getAction() : "GENERIC_ACTION")
                .entityType(dto.getEntityType() != null ? dto.getEntityType() : "GENERIC_ENTITY")
                .entityId(dto.getEntityId())
                .correlationId(dto.getCorrelationId())
                .outcome(dto.getOutcome() != null ? dto.getOutcome() : "SUCCESS")
                .beforeState(dto.getBeforeState())
                .afterState(dto.getAfterState())
                .metadata(metadataJson)
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now())
                .build();

        AuditEvent saved = auditRepository.save(event);
        log.info("[Audit] Recorded audit event id={} action={} entityType={} actor={}",
                saved.getId(), saved.getAction(), saved.getEntityType(), saved.getActorId());

        return mapper.toEventResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditEventResponse> getEvents(
            String actorId, String action, String entityType, String correlationId,
            Instant from, Instant to, Pageable pageable) {

        Specification<AuditEvent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (actorId != null && !actorId.isBlank()) {
                predicates.add(cb.equal(root.get("actorId"), actorId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (correlationId != null && !correlationId.isBlank()) {
                predicates.add(cb.equal(root.get("correlationId"), correlationId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditEvent> page = auditRepository.findAll(spec, pageable);
        List<AuditEventResponse> content = page.getContent().stream()
                .map(mapper::toEventResponse)
                .collect(Collectors.toList());

        return PagedResponse.<AuditEventResponse>builder()
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
    public AuditEventResponse getEventById(Long id) {
        AuditEvent event = auditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit event not found with id: " + id));
        return mapper.toEventResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditSummaryStatsResponse getAuditStats() {
        long total = auditRepository.count();
        long success = auditRepository.countByOutcome("SUCCESS");
        long failure = auditRepository.countByOutcome("FAILURE");

        Map<String, Long> byActorType = new HashMap<>();
        List<Object[]> actorRows = auditRepository.countGroupedByActorType();
        for (Object[] row : actorRows) {
            String type = (String) row[0];
            Long count = (Long) row[1];
            byActorType.put(type, count);
        }

        Map<String, Long> topActions = new LinkedHashMap<>();
        List<Object[]> actionRows = auditRepository.countTopActions(PageRequest.of(0, 5));
        for (Object[] row : actionRows) {
            String act = (String) row[0];
            Long count = (Long) row[1];
            topActions.put(act, count);
        }

        return AuditSummaryStatsResponse.builder()
                .totalEvents(total)
                .successCount(success)
                .failureCount(failure)
                .eventsByActorType(byActorType)
                .topActions(topActions)
                .build();
    }
}
