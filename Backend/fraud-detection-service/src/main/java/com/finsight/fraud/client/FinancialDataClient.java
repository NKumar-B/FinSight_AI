package com.finsight.fraud.client;

import com.finsight.common.dto.ApiResponse;
import com.finsight.fraud.client.dto.TransactionSummaryDto;
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

    @GetMapping("/api/v1/transactions/history")
    ApiResponse<List<TransactionSummaryDto>> getTransactionHistory(@RequestParam("accountId") Long accountId);

    @Component
    class FinancialDataClientFallback implements FinancialDataClient {
        private static final Logger log = LoggerFactory.getLogger(FinancialDataClientFallback.class);

        @Override
        public ApiResponse<List<TransactionSummaryDto>> getTransactionHistory(Long accountId) {
            log.warn("[FinancialDataClient Fallback] Financial data service unavailable for accountId={}", accountId);
            return ApiResponse.ok(Collections.emptyList(), "Fallback empty transaction history");
        }
    }
}
