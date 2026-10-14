package com.finsight.insights.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.security.JwtUtils;
import com.finsight.insights.client.AuditClient;
import com.finsight.insights.client.FinancialDataClient;
import com.finsight.insights.dto.request.CreateInvoiceRequest;
import com.finsight.insights.dto.response.AccountsReceivableAgingResponse;
import com.finsight.insights.dto.response.InvoiceResponse;
import com.finsight.insights.enums.InvoiceStatus;
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
import java.time.LocalDate;

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
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private FinancialDataClient financialDataClient;

    @MockBean
    private AuditClient auditClient;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "accountant", roles = {"ACCOUNTANT"})
    void createInvoice_authorized_returnsCreated() throws Exception {
        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .invoiceNumber("INV-100")
                .customerId(1L)
                .customerName("Client A")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .amount(new BigDecimal("2500.00"))
                .build();

        InvoiceResponse response = InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-100")
                .customerId(1L)
                .customerName("Client A")
                .amount(new BigDecimal("2500.00"))
                .balanceDue(new BigDecimal("2500.00"))
                .status(InvoiceStatus.ISSUED)
                .build();

        when(invoiceService.createInvoice(any(), eq("accountant"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/insights/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV-100"));
    }

    @Test
    @WithMockUser(username = "analyst", roles = {"FINANCIAL_ANALYST"})
    void getArAging_authorized_returnsOk() throws Exception {
        AccountsReceivableAgingResponse aging = AccountsReceivableAgingResponse.builder()
                .totalOutstanding(new BigDecimal("15000.00"))
                .currentBucket(new BigDecimal("10000.00"))
                .bucket31To60(new BigDecimal("5000.00"))
                .totalInvoicesCount(5)
                .build();

        when(invoiceService.getArAging()).thenReturn(aging);

        mockMvc.perform(get("/api/v1/insights/invoices/aging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOutstanding").value(15000.00));
    }
}
