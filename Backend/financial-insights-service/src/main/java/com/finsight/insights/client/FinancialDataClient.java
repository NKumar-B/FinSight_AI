package com.finsight.insights.client;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import com.finsight.insights.client.dto.AccountSummaryDto;
import com.finsight.insights.client.dto.TransactionDetailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@FeignClient(name = "financial-data-service", fallback = FinancialDataClient.FinancialDataClientFallback.class)
public interface FinancialDataClient {

    @GetMapping("/api/v1/accounts")
    ApiResponse<PagedResponse<AccountSummaryDto>> getAccounts(@RequestParam(name = "size", defaultValue = "100") int size);

    @GetMapping("/api/v1/transactions")
    ApiResponse<PagedResponse<TransactionDetailDto>> getTransactions(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "size", defaultValue = "500") int size);

    @Component
    class FinancialDataClientFallback implements FinancialDataClient {
        private static final Logger log = LoggerFactory.getLogger(FinancialDataClientFallback.class);

        @Override
        public ApiResponse<PagedResponse<AccountSummaryDto>> getAccounts(int size) {
            log.warn("[FinancialDataClient Fallback] Fallback invoked for getAccounts");
            return ApiResponse.ok(PagedResponse.empty(0, size), "Fallback accounts response");
        }

        @Override
        public ApiResponse<PagedResponse<TransactionDetailDto>> getTransactions(String type, int size) {
            log.warn("[FinancialDataClient Fallback] Fallback invoked for getTransactions");
            return ApiResponse.ok(PagedResponse.empty(0, size), "Fallback transactions response");
        }
    }
}
