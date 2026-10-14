package com.finsight.audit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.audit.dto.response.AuditEventResponse;
import com.finsight.audit.dto.response.AuditSummaryStatsResponse;
import com.finsight.audit.service.AuditService;
import com.finsight.audit.service.ComplianceReportService;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditService auditService;

    @MockBean
    private ComplianceReportService reportService;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void recordEvent_unauthenticatedServiceCall_returnsCreated() throws Exception {
        AuditEventDto dto = AuditEventDto.builder()
                .actorId("financial-service")
                .actorType("SYSTEM")
                .action("TRANSACTION_POST")
                .entityType("TRANSACTION")
                .entityId("2001")
                .correlationId("corr-test")
                .outcome("SUCCESS")
                .build();

        AuditEventResponse response = AuditEventResponse.builder()
                .id(1L)
                .actorId("financial-service")
                .action("TRANSACTION_POST")
                .entityType("TRANSACTION")
                .outcome("SUCCESS")
                .timestamp(Instant.now())
                .build();

        when(auditService.recordEvent(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.action").value("TRANSACTION_POST"));
    }

    @Test
    @WithMockUser(username = "auditor", roles = {"AUDITOR"})
    void getEvents_authorized_returnsOk() throws Exception {
        when(auditService.getEvents(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(PagedResponse.empty(0, 20));

        mockMvc.perform(get("/api/v1/audit/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @WithMockUser(username = "auditor", roles = {"AUDITOR"})
    void getAuditStats_authorized_returnsOk() throws Exception {
        AuditSummaryStatsResponse stats = AuditSummaryStatsResponse.builder()
                .totalEvents(50)
                .successCount(48)
                .failureCount(2)
                .eventsByActorType(Map.of("USER", 30L, "SYSTEM", 20L))
                .build();

        when(auditService.getAuditStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/audit/events/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEvents").value(50));
    }
}
