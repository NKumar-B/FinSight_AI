package com.finsight.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.audit.dto.request.GenerateReportRequest;
import com.finsight.audit.dto.response.ComplianceReportResponse;
import com.finsight.audit.entity.AuditEvent;
import com.finsight.audit.entity.ComplianceReport;
import com.finsight.audit.mapper.AuditMapper;
import com.finsight.audit.repository.AuditEventRepository;
import com.finsight.audit.repository.ComplianceReportRepository;
import com.finsight.audit.service.impl.ComplianceReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplianceReportServiceTest {

    @Mock
    private ComplianceReportRepository reportRepository;

    @Mock
    private AuditEventRepository auditRepository;

    @Spy
    private AuditMapper mapper = new AuditMapper();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ComplianceReportServiceImpl reportService;

    @Test
    void generateReport_success() {
        GenerateReportRequest request = GenerateReportRequest.builder()
                .reportType("COMPLIANCE_SUMMARY")
                .startDate(Instant.now().minusSeconds(86400))
                .endDate(Instant.now())
                .build();

        List<AuditEvent> events = List.of(
                AuditEvent.builder().id(1L).action("USER_LOGIN").outcome("SUCCESS").build(),
                AuditEvent.builder().id(2L).action("TRANSACTION_POST").outcome("SUCCESS").build()
        );

        when(auditRepository.findAll(any(Specification.class))).thenReturn(events);
        when(reportRepository.save(any(ComplianceReport.class))).thenAnswer(i -> {
            ComplianceReport r = i.getArgument(0);
            r.setId(10L);
            return r;
        });

        ComplianceReportResponse response = reportService.generateReport(request, "compliance_officer");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getReportReference()).startsWith("RPT-");
        assertThat(response.getReportType()).isEqualTo("COMPLIANCE_SUMMARY");
        assertThat(response.getTotalEventsCount()).isEqualTo(2);
        assertThat(response.getGeneratedBy()).isEqualTo("compliance_officer");
    }
}
