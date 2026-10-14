package com.finsight.insights.service;

import com.finsight.insights.dto.request.CashFlowForecastRequest;
import com.finsight.insights.dto.response.CashFlowForecastResponse;
import com.finsight.insights.dto.response.ExpenseBreakdownResponse;

import java.time.LocalDate;

public interface FinancialAnalyticsService {
    ExpenseBreakdownResponse getExpenseAnalysis(LocalDate from, LocalDate to);
    CashFlowForecastResponse getCashFlowForecast(CashFlowForecastRequest request);
}
