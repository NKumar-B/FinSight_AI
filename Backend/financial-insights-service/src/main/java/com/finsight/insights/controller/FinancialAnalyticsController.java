package com.finsight.insights.controller;

import com.finsight.common.dto.ApiResponse;
import com.finsight.insights.dto.request.CashFlowForecastRequest;
import com.finsight.insights.dto.response.CashFlowForecastResponse;
import com.finsight.insights.dto.response.ExpenseBreakdownResponse;
import com.finsight.insights.service.FinancialAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/insights/analytics")
@RequiredArgsConstructor
@Tag(name = "Financial Analytics", description = "Endpoints for expense categorization, vendor analysis, and cash-flow forecasting")
public class FinancialAnalyticsController {

    private final FinancialAnalyticsService analyticsService;

    @GetMapping("/expenses")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'ACCOUNTANT', 'AUDITOR')")
    @Operation(summary = "Get expense categorization", description = "Categorizes historical expenses, calculates category percentages, and surfaces top vendors")
    public ResponseEntity<ApiResponse<ExpenseBreakdownResponse>> getExpenseAnalysis(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.getExpenseAnalysis(from, to),
                "Expense analysis calculated successfully"));
    }

    @PostMapping("/cash-flow/forecast")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'ACCOUNTANT')")
    @Operation(summary = "Generate cash-flow forecast", description = "Simulates cash balance trajectory over N days incorporating AR collections and recurring flows")
    public ResponseEntity<ApiResponse<CashFlowForecastResponse>> getCashFlowForecast(
            @Valid @RequestBody(required = false) CashFlowForecastRequest request) {
        CashFlowForecastRequest req = request != null ? request : new CashFlowForecastRequest();
        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.getCashFlowForecast(req),
                "Cash-flow forecast calculated successfully"));
    }
}
