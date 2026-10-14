package com.finsight.orchestrator.client;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@FeignClient(name = "financial-data-service", fallback = FinancialDataClient.FinancialDataClientFallback.class)
public interface FinancialDataClient {

    @GetMapping("/api/v1/accounts")
    ApiResponse<PagedResponse<Map<String, Object>>> getAccounts(@RequestParam(name = "size", defaultValue = "50") int size);

    @GetMapping("/api/v1/transactions")
    ApiResponse<PagedResponse<Map<String, Object>>> getTransactions(@RequestParam(name = "size", defaultValue = "50") int size);

    @GetMapping("/api/v1/transactions/unreconciled")
    ApiResponse<List<Map<String, Object>>> getUnreconciledTransactions();

    @Component
    class FinancialDataClientFallback implements FinancialDataClient {
        private static final Logger log = LoggerFactory.getLogger(FinancialDataClientFallback.class);

        @Override
        public ApiResponse<PagedResponse<Map<String, Object>>> getAccounts(int size) {
            log.warn("[FinancialDataClient Fallback] Fallback invoked for getAccounts");
            return ApiResponse.ok(PagedResponse.empty(0, size), "Fallback accounts response");
        }

        @Override
        public ApiResponse<PagedResponse<Map<String, Object>>> getTransactions(int size) {
            log.warn("[FinancialDataClient Fallback] Fallback invoked for getTransactions");
            return ApiResponse.ok(PagedResponse.empty(0, size), "Fallback transactions response");
        }

        @Override
        public ApiResponse<List<Map<String, Object>>> getUnreconciledTransactions() {
            log.warn("[FinancialDataClient Fallback] Fallback invoked for getUnreconciledTransactions");
            return ApiResponse.ok(Collections.emptyList(), "Fallback unreconciled list");
        }
    }
}
