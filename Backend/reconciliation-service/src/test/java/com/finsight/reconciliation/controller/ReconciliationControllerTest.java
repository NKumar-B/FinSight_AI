package com.finsight.reconciliation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.security.JwtUtils;
import com.finsight.reconciliation.client.AuditClient;
import com.finsight.reconciliation.client.FinancialDataClient;
import com.finsight.reconciliation.dto.request.ExecuteReconciliationRequest;
import com.finsight.reconciliation.dto.response.ReconciliationRunResponse;
import com.finsight.reconciliation.enums.ReconciliationRunStatus;
import com.finsight.reconciliation.service.ReconciliationReviewService;
import com.finsight.reconciliation.service.ReconciliationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReconciliationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReconciliationService reconciliationService;

    @MockBean
    private ReconciliationReviewService reviewService;

    @MockBean
    private FinancialDataClient financialDataClient;

    @MockBean
    private AuditClient auditClient;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "analyst", roles = {"FINANCIAL_ANALYST"})
    void executeReconciliation_authorized_returnsCreated() throws Exception {
        ReconciliationRunResponse response = ReconciliationRunResponse.builder()
                .id(1L)
                .runReference("REC-12345678")
                .status(ReconciliationRunStatus.COMPLETED)
                .totalSourceRecords(5)
                .totalTargetRecords(5)
                .exactMatchesCount(4)
                .toleranceMatchesCount(1)
                .discrepanciesCount(0)
                .dateToleranceDays(2)
                .amountTolerance(new BigDecimal("0.05"))
                .executedBy("analyst")
                .createdAt(Instant.now())
                .completedAt(Instant.now())
                .build();

        when(reconciliationService.executeReconciliation(any(), eq("analyst"))).thenReturn(response);

        ExecuteReconciliationRequest request = ExecuteReconciliationRequest.builder()
                .dateToleranceDays(2)
                .amountTolerance(new BigDecimal("0.05"))
                .build();

        mockMvc.perform(post("/api/v1/reconciliation/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.runReference").value("REC-12345678"))
                .andExpect(jsonPath("$.data.exactMatchesCount").value(4));
    }

    @Test
    @WithMockUser(username = "auditor", roles = {"AUDITOR"})
    void listRuns_authorized_returnsOk() throws Exception {
        PagedResponse<ReconciliationRunResponse> pagedResponse = PagedResponse.empty(0, 20);

        when(reconciliationService.listRuns(any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/reconciliation/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }
}
