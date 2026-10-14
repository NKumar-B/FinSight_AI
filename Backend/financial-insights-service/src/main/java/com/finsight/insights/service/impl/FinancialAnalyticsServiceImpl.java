package com.finsight.insights.service.impl;

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
import com.finsight.insights.service.FinancialAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialAnalyticsServiceImpl implements FinancialAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(FinancialAnalyticsServiceImpl.class);

    private final FinancialDataClient financialDataClient;
    private final InvoiceRepository invoiceRepository;

    @Override
    public ExpenseBreakdownResponse getExpenseAnalysis(LocalDate from, LocalDate to) {
        log.info("[Analytics] Generating expense analysis between {} and {}", from, to);

        List<TransactionDetailDto> transactions = Collections.emptyList();
        try {
            ApiResponse<PagedResponse<TransactionDetailDto>> resp = financialDataClient.getTransactions("DEBIT", 500);
            if (resp != null && resp.getData() != null && resp.getData().getContent() != null) {
                transactions = resp.getData().getContent();
            }
        } catch (Exception e) {
            log.warn("[Analytics] Could not fetch transactions from financial-data-service: {}", e.getMessage());
        }

        List<TransactionDetailDto> filtered = transactions.stream()
                .filter(t -> t.getAmount() != null)
                .filter(t -> from == null || (t.getTransactionDate() != null && !t.getTransactionDate().isBefore(from)))
                .filter(t -> to == null || (t.getTransactionDate() != null && !t.getTransactionDate().isAfter(to)))
                .toList();

        BigDecimal totalExpense = filtered.stream()
                .map(TransactionDetailDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = filtered.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null && !t.getCategory().isBlank() ? t.getCategory().toUpperCase() : "OTHER",
                        Collectors.reducing(BigDecimal.ZERO, TransactionDetailDto::getAmount, BigDecimal::add)
                ));

        Map<String, Double> categoryPercentages = new LinkedHashMap<>();
        if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<String, BigDecimal> entry : byCategory.entrySet()) {
                double pct = entry.getValue().divide(totalExpense, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                categoryPercentages.put(entry.getKey(), Math.round(pct * 10.0) / 10.0);
            }
        }

        Map<String, BigDecimal> topMerchants = filtered.stream()
                .filter(t -> t.getMerchantName() != null && !t.getMerchantName().isBlank())
                .collect(Collectors.groupingBy(
                        TransactionDetailDto::getMerchantName,
                        Collectors.reducing(BigDecimal.ZERO, TransactionDetailDto::getAmount, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return ExpenseBreakdownResponse.builder()
                .totalExpense(totalExpense)
                .currency("USD")
                .byCategory(byCategory)
                .categoryPercentages(categoryPercentages)
                .topMerchants(topMerchants)
                .totalTransactionsAnalyzed(filtered.size())
                .build();
    }

    @Override
    public CashFlowForecastResponse getCashFlowForecast(CashFlowForecastRequest request) {
        log.info("[Analytics] Calculating cash-flow forecast for {} days", request.getDays());

        int days = request.getDays() > 0 ? request.getDays() : 30;

        // 1. Fetch current cash reserves from Financial Data Service
        BigDecimal currentCashBalance = BigDecimal.ZERO;
        try {
            ApiResponse<PagedResponse<AccountSummaryDto>> accResp = financialDataClient.getAccounts(100);
            if (accResp != null && accResp.getData() != null && accResp.getData().getContent() != null) {
                currentCashBalance = accResp.getData().getContent().stream()
                        .filter(a -> a.getCurrentBalance() != null)
                        .map(AccountSummaryDto::getCurrentBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } catch (Exception e) {
            log.warn("[Analytics] Could not fetch accounts from financial-data-service: {}", e.getMessage());
        }

        // If no balances returned, default to baseline
        if (currentCashBalance.compareTo(BigDecimal.ZERO) == 0) {
            currentCashBalance = new BigDecimal("100000.00");
        }

        // 2. Scheduled AR Collections from outstanding invoices
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(days);
        List<Invoice> outstanding = invoiceRepository.findOutstandingInvoices();

        Map<LocalDate, BigDecimal> invoiceInflowsByDate = new HashMap<>();
        BigDecimal totalInvoiceInflows = BigDecimal.ZERO;

        for (Invoice inv : outstanding) {
            if (inv.getDueDate() != null && !inv.getDueDate().isBefore(today) && !inv.getDueDate().isAfter(horizon)) {
                BigDecimal balanceDue = inv.getAmount().subtract(inv.getPaidAmount() != null ? inv.getPaidAmount() : BigDecimal.ZERO).max(BigDecimal.ZERO);
                invoiceInflowsByDate.merge(inv.getDueDate(), balanceDue, BigDecimal::add);
                totalInvoiceInflows = totalInvoiceInflows.add(balanceDue);
            }
        }

        // 3. Recurring Daily Inflows & Outflows
        BigDecimal monthlyInflow = request.getMonthlyRecurringInflow() != null ? request.getMonthlyRecurringInflow() : BigDecimal.ZERO;
        BigDecimal dailyRecurringInflow = monthlyInflow.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

        BigDecimal monthlyOutflow = request.getMonthlyRecurringOutflow() != null ? request.getMonthlyRecurringOutflow() : BigDecimal.ZERO;
        BigDecimal dailyRecurringOutflow = monthlyOutflow.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

        // 4. Build daily trajectory
        BigDecimal runningBalance = currentCashBalance;
        BigDecimal totalProjectedInflows = totalInvoiceInflows.add(dailyRecurringInflow.multiply(BigDecimal.valueOf(days)));
        BigDecimal totalProjectedOutflows = dailyRecurringOutflow.multiply(BigDecimal.valueOf(days));

        List<CashFlowForecastResponse.DailyCashTrajectoryPoint> trajectory = new ArrayList<>();

        for (int i = 1; i <= days; i++) {
            LocalDate day = today.plusDays(i);
            BigDecimal dayInvoiceInflow = invoiceInflowsByDate.getOrDefault(day, BigDecimal.ZERO);
            BigDecimal dayNet = dayInvoiceInflow.add(dailyRecurringInflow).subtract(dailyRecurringOutflow);

            runningBalance = runningBalance.add(dayNet);
            trajectory.add(CashFlowForecastResponse.DailyCashTrajectoryPoint.builder()
                    .date(day)
                    .projectedBalance(runningBalance)
                    .dailyNetChange(dayNet)
                    .build());
        }

        BigDecimal netProjectedCashFlow = totalProjectedInflows.subtract(totalProjectedOutflows);
        BigDecimal burnRatePerDay = totalProjectedOutflows.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);

        Long runwayDays = null;
        if (dailyRecurringOutflow.compareTo(dailyRecurringInflow) > 0) {
            BigDecimal dailyDeficit = dailyRecurringOutflow.subtract(dailyRecurringInflow);
            if (dailyDeficit.compareTo(BigDecimal.ZERO) > 0) {
                runwayDays = currentCashBalance.divide(dailyDeficit, 0, RoundingMode.DOWN).longValue();
            }
        }

        return CashFlowForecastResponse.builder()
                .currentCashBalance(currentCashBalance)
                .forecastDays(days)
                .projectedInflows(totalProjectedInflows)
                .projectedOutflows(totalProjectedOutflows)
                .netProjectedCashFlow(netProjectedCashFlow)
                .projectedClosingBalance(runningBalance)
                .estimatedBurnRatePerDay(burnRatePerDay)
                .runwayDays(runwayDays)
                .dailyTrajectory(trajectory)
                .build();
    }
}
