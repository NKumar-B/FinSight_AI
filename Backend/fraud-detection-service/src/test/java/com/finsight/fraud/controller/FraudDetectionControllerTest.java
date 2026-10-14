package com.finsight.fraud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.security.JwtUtils;
import com.finsight.fraud.client.AuditClient;
import com.finsight.fraud.client.FinancialDataClient;
import com.finsight.fraud.dto.request.EvaluateTransactionRequest;
import com.finsight.fraud.dto.response.FraudAlertStatsResponse;
import com.finsight.fraud.dto.response.FraudEvaluationResponse;
import com.finsight.fraud.enums.RiskSeverity;
import com.finsight.fraud.service.FraudDetectionService;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FraudDetectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FraudDetectionService fraudService;

    @MockBean
    private FinancialDataClient financialDataClient;

    @MockBean
    private AuditClient auditClient;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "investigator", roles = {"FRAUD_INVESTIGATOR"})
    void evaluateTransaction_authorized_returnsOk() throws Exception {
        EvaluateTransactionRequest request = EvaluateTransactionRequest.builder()
                .transactionId(101L)
                .accountId(1L)
                .transactionReference("TX-EVAL-1")
                .amount(new BigDecimal("25000.00"))
                .currency("USD")
                .transactionDate(LocalDate.of(2026, 4, 10))
                .build();

        FraudEvaluationResponse response = FraudEvaluationResponse.builder()
                .transactionId(101L)
                .riskScore(40)
                .severity(RiskSeverity.MEDIUM)
                .alertGenerated(false)
                .triggeredRules(List.of("HIGH_AMOUNT_ANOMALY"))
                .details("High transaction amount")
                .build();

        when(fraudService.evaluateTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/fraud/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.riskScore").value(40))
                .andExpect(jsonPath("$.data.severity").value("MEDIUM"));
    }

    @Test
    @WithMockUser(username = "compliance", roles = {"COMPLIANCE_OFFICER"})
    void listAlerts_authorized_returnsOk() throws Exception {
        when(fraudService.listAlerts(any(), any(), any())).thenReturn(PagedResponse.empty(0, 20));

        mockMvc.perform(get("/api/v1/fraud/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @WithMockUser(username = "auditor", roles = {"AUDITOR"})
    void getAlertStats_authorized_returnsStats() throws Exception {
        FraudAlertStatsResponse stats = FraudAlertStatsResponse.builder()
                .totalAlerts(12)
                .newAlerts(5)
                .underReviewAlerts(3)
                .confirmedFraudAlerts(4)
                .criticalAlerts(2)
                .highAlerts(5)
                .build();

        when(fraudService.getAlertStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/fraud/alerts/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAlerts").value(12))
                .andExpect(jsonPath("$.data.confirmedFraudAlerts").value(4));
    }
}
