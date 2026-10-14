package com.finsight.insights.service;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import com.finsight.insights.client.FinancialDataClient;
import com.finsight.insights.client.dto.AccountSummaryDto;
import com.finsight.insights.client.dto.TransactionDetailDto;
import com.finsight.insights.dto.request.CashFlowForecastRequest;
import com.finsight.insights.dto.response.CashFlowForecastResponse;
import com.finsight.insights.dto.response.ExpenseBreakdownResponse;
import com.finsight.insights.entity.Invoice;
import com.finsight.insights.repository.InvoiceRepository;
import com.finsight.insights.service.impl.FinancialAnalyticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialAnalyticsServiceTest {

    @Mock
    private FinancialDataClient financialDataClient;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private FinancialAnalyticsServiceImpl analyticsService;

    @Test
    void getExpenseAnalysis_calculatesCategoriesAndPercentages() {
        LocalDate now = LocalDate.now();
        List<TransactionDetailDto> transactions = List.of(
                TransactionDetailDto.builder().id(1L).amount(new BigDecimal("600.00")).category("SOFTWARE_SAAS").merchantName("AWS").transactionDate(now).build(),
                TransactionDetailDto.builder().id(2L).amount(new BigDecimal("400.00")).category("MARKETING").merchantName("Google Ads").transactionDate(now).build()
        );

        PagedResponse<TransactionDetailDto> paged = PagedResponse.<TransactionDetailDto>builder()
                .content(transactions)
                .build();

        when(financialDataClient.getTransactions(anyString(), anyInt()))
                .thenReturn(ApiResponse.ok(paged, "Transactions"));

        ExpenseBreakdownResponse response = analyticsService.getExpenseAnalysis(now.minusDays(7), now);

        assertThat(response).isNotNull();
        assertThat(response.getTotalExpense()).isEqualByComparingTo("1000.00");
        assertThat(response.getByCategory()).containsEntry("SOFTWARE_SAAS", new BigDecimal("600.00"));
        assertThat(response.getByCategory()).containsEntry("MARKETING", new BigDecimal("400.00"));
        assertThat(response.getCategoryPercentages()).containsEntry("SOFTWARE_SAAS", 60.0);
        assertThat(response.getCategoryPercentages()).containsEntry("MARKETING", 40.0);
        assertThat(response.getTopMerchants()).containsEntry("AWS", new BigDecimal("600.00"));
    }

    @Test
    void getCashFlowForecast_computesTrajectoryAndRunway() {
        LocalDate today = LocalDate.now();

        // Account with $50,000 cash balance
        AccountSummaryDto account = AccountSummaryDto.builder()
                .id(1L)
                .currentBalance(new BigDecimal("50000.00"))
                .build();
        PagedResponse<AccountSummaryDto> pagedAcc = PagedResponse.<AccountSummaryDto>builder()
                .content(List.of(account))
                .build();
        when(financialDataClient.getAccounts(anyInt())).thenReturn(ApiResponse.ok(pagedAcc, "Accounts"));

        // Invoice due in 5 days for $10,000
        Invoice invoice = Invoice.builder()
                .id(1L)
                .amount(new BigDecimal("10000.00"))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(today.plusDays(5))
                .build();
        when(invoiceRepository.findOutstandingInvoices()).thenReturn(List.of(invoice));

        CashFlowForecastRequest request = CashFlowForecastRequest.builder()
                .days(30)
                .monthlyRecurringInflow(new BigDecimal("6000.00"))   // $200/day
                .monthlyRecurringOutflow(new BigDecimal("15000.00")) // $500/day
                .build();

        CashFlowForecastResponse forecast = analyticsService.getCashFlowForecast(request);

        assertThat(forecast).isNotNull();
        assertThat(forecast.getCurrentCashBalance()).isEqualByComparingTo("50000.00");
        assertThat(forecast.getForecastDays()).isEqualTo(30);
        assertThat(forecast.getDailyTrajectory()).hasSize(30);
        assertThat(forecast.getRunwayDays()).isNotNull();
        assertThat(forecast.getRunwayDays()).isGreaterThan(0);
    }
}
