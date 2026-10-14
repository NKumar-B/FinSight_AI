package com.finsight.insights.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.common.security.JwtUtils;
import com.finsight.insights.client.AuditClient;
import com.finsight.insights.client.FinancialDataClient;
import com.finsight.insights.dto.request.CashFlowForecastRequest;
import com.finsight.insights.dto.response.CashFlowForecastResponse;
import com.finsight.insights.dto.response.ExpenseBreakdownResponse;
import com.finsight.insights.service.FinancialAnalyticsService;
import com.finsight.insights.service.InvoiceService;
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
import java.util.Collections;
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
class FinancialAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FinancialAnalyticsService analyticsService;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private FinancialDataClient financialDataClient;

    @MockBean
    private AuditClient auditClient;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "analyst", roles = {"FINANCIAL_ANALYST"})
    void getExpenseAnalysis_authorized_returnsOk() throws Exception {
        ExpenseBreakdownResponse response = ExpenseBreakdownResponse.builder()
                .totalExpense(new BigDecimal("12500.00"))
                .currency("USD")
                .byCategory(Map.of("SOFTWARE_SAAS", new BigDecimal("5000.00")))
                .categoryPercentages(Map.of("SOFTWARE_SAAS", 40.0))
                .topMerchants(Map.of("AWS", new BigDecimal("5000.00")))
                .totalTransactionsAnalyzed(25)
                .build();

        when(analyticsService.getExpenseAnalysis(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/insights/analytics/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalExpense").value(12500.00));
    }

    @Test
    @WithMockUser(username = "analyst", roles = {"FINANCIAL_ANALYST"})
    void getCashFlowForecast_authorized_returnsOk() throws Exception {
        CashFlowForecastResponse response = CashFlowForecastResponse.builder()
                .currentCashBalance(new BigDecimal("100000.00"))
                .forecastDays(30)
                .projectedInflows(new BigDecimal("20000.00"))
                .projectedOutflows(new BigDecimal("15000.00"))
                .netProjectedCashFlow(new BigDecimal("5000.00"))
                .projectedClosingBalance(new BigDecimal("105000.00"))
                .dailyTrajectory(Collections.emptyList())
                .build();

        when(analyticsService.getCashFlowForecast(any())).thenReturn(response);

        CashFlowForecastRequest request = CashFlowForecastRequest.builder()
                .days(30)
                .build();

        mockMvc.perform(post("/api/v1/insights/analytics/cash-flow/forecast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.netProjectedCashFlow").value(5000.00));
    }
}
