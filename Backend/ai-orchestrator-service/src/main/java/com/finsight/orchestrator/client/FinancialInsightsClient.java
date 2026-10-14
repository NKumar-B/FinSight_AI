package com.finsight.orchestrator.client;

import com.finsight.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "financial-insights-service", fallback = FinancialInsightsClient.FinancialInsightsClientFallback.class)
public interface FinancialInsightsClient {

    @GetMapping("/api/v1/insights/analytics/expenses")
    ApiResponse<Map<String, Object>> getExpenseAnalysis();

    @PostMapping("/api/v1/insights/analytics/cash-flow/forecast")
    ApiResponse<Map<String, Object>> getCashFlowForecast(@RequestBody Map<String, Object> request);

    @GetMapping("/api/v1/insights/invoices/aging")
    ApiResponse<Map<String, Object>> getArAging();

    @Component
    class FinancialInsightsClientFallback implements FinancialInsightsClient {
        private static final Logger log = LoggerFactory.getLogger(FinancialInsightsClientFallback.class);

        @Override
        public ApiResponse<Map<String, Object>> getExpenseAnalysis() {
            log.warn("[FinancialInsightsClient Fallback] Fallback for getExpenseAnalysis");
            return ApiResponse.ok(Map.of("totalExpense", 0), "Fallback expense response");
        }

        @Override
        public ApiResponse<Map<String, Object>> getCashFlowForecast(Map<String, Object> request) {
            log.warn("[FinancialInsightsClient Fallback] Fallback for getCashFlowForecast");
            return ApiResponse.ok(Map.of("projectedClosingBalance", 0), "Fallback forecast response");
        }

        @Override
        public ApiResponse<Map<String, Object>> getArAging() {
            log.warn("[FinancialInsightsClient Fallback] Fallback for getArAging");
            return ApiResponse.ok(Map.of("totalOutstanding", 0), "Fallback AR aging response");
        }
    }
}
