package com.finsight.audit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.audit.dto.request.GenerateReportRequest;
import com.finsight.audit.dto.response.ComplianceReportResponse;
import com.finsight.audit.entity.AuditEvent;
import com.finsight.audit.entity.ComplianceReport;
import com.finsight.audit.mapper.AuditMapper;
import com.finsight.audit.repository.AuditEventRepository;
import com.finsight.audit.repository.ComplianceReportRepository;
import com.finsight.audit.service.ComplianceReportService;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplianceReportServiceImpl implements ComplianceReportService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceReportServiceImpl.class);

    private final ComplianceReportRepository reportRepository;
    private final AuditEventRepository auditRepository;
    private final AuditMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ComplianceReportResponse generateReport(GenerateReportRequest request, String generatedBy) {
        log.info("[Compliance Reporting] Generating report type={} for user={}", request.getReportType(), generatedBy);

        Specification<AuditEvent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), request.getEndDate()));
            }
            if (request.getEntityType() != null && !request.getEntityType().isBlank()) {
                predicates.add(cb.equal(root.get("entityType"), request.getEntityType()));
            }
            if (request.getActorId() != null && !request.getActorId().isBlank()) {
                predicates.add(cb.equal(root.get("actorId"), request.getActorId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<AuditEvent> matchingEvents = auditRepository.findAll(spec);
        int totalEvents = matchingEvents.size();

        Map<String, Long> actionsBreakdown = matchingEvents.stream()
                .collect(Collectors.groupingBy(AuditEvent::getAction, Collectors.counting()));

        Map<String, Long> outcomesBreakdown = matchingEvents.stream()
                .collect(Collectors.groupingBy(AuditEvent::getOutcome, Collectors.counting()));

        Map<String, Object> summaryMap = new LinkedHashMap<>();
        summaryMap.put("reportType", request.getReportType());
        summaryMap.put("generatedAt", Instant.now().toString());
        summaryMap.put("generatedBy", generatedBy);
        summaryMap.put("totalEventsAnalyzed", totalEvents);
        summaryMap.put("actionsBreakdown", actionsBreakdown);
        summaryMap.put("outcomesBreakdown", outcomesBreakdown);

        String summaryJson;
        try {
            summaryJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(summaryMap);
        } catch (Exception e) {
            summaryJson = "Total events: " + totalEvents;
        }

        String paramsJson = "";
        try {
            paramsJson = objectMapper.writeValueAsString(request);
        } catch (Exception ignored) {}

        String reportRef = "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ComplianceReport report = ComplianceReport.builder()
                .reportReference(reportRef)
                .reportType(request.getReportType())
                .generatedBy(generatedBy)
                .parameters(paramsJson)
                .totalEventsCount(totalEvents)
                .summaryContent(summaryJson)
                .build();

        ComplianceReport saved = reportRepository.save(report);
        log.info("[Compliance Reporting] Report generated: ref={} totalEvents={}", reportRef, totalEvents);

        return mapper.toReportResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ComplianceReportResponse> listReports(Pageable pageable) {
        Page<ComplianceReport> page = reportRepository.findAll(pageable);
        List<ComplianceReportResponse> content = page.getContent().stream()
                .map(mapper::toReportResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ComplianceReportResponse>builder()
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
    public ComplianceReportResponse getReportById(Long id) {
        ComplianceReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance report not found with id: " + id));
        return mapper.toReportResponse(report);
    }
}
