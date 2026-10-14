package com.finsight.reconciliation.client;

import com.finsight.common.dto.ApiResponse;
import com.finsight.reconciliation.client.dto.ExternalTransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@FeignClient(name = "financial-data-service", fallback = FinancialDataClient.FinancialDataClientFallback.class)
public interface FinancialDataClient {

    @GetMapping("/api/v1/transactions/unreconciled")
    ApiResponse<List<ExternalTransactionDto>> getUnreconciledTransactions();

    @PatchMapping("/api/v1/transactions/{id}/status")
    ApiResponse<ExternalTransactionDto> updateTransactionStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> updateRequest);

    @Component
    class FinancialDataClientFallback implements FinancialDataClient {
        private static final Logger log = LoggerFactory.getLogger(FinancialDataClientFallback.class);

        @Override
        public ApiResponse<List<ExternalTransactionDto>> getUnreconciledTransactions() {
            log.warn("[FinancialDataClient Fallback] Financial data service unavailable when fetching unreconciled transactions");
            return ApiResponse.ok(Collections.emptyList(), "Fallback empty unreconciled list");
        }

        @Override
        public ApiResponse<ExternalTransactionDto> updateTransactionStatus(Long id, Map<String, Object> updateRequest) {
            log.warn("[FinancialDataClient Fallback] Failed to update status for transaction id={}", id);
            return ApiResponse.ok(null, "Fallback executed for status update");
        }
    }
}
