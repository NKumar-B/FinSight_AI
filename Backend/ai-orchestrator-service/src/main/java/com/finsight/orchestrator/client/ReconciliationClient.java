package com.finsight.orchestrator.client;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "reconciliation-service", fallback = ReconciliationClient.ReconciliationClientFallback.class)
public interface ReconciliationClient {

    @PostMapping("/api/v1/reconciliation/runs")
    ApiResponse<Map<String, Object>> executeReconciliation(@RequestBody Map<String, Object> request);

    @GetMapping("/api/v1/reconciliation/runs")
    ApiResponse<PagedResponse<Map<String, Object>>> listRuns(@RequestParam(name = "size", defaultValue = "20") int size);

    @GetMapping("/api/v1/reconciliation/review/cases")
    ApiResponse<PagedResponse<Map<String, Object>>> listDiscrepancyCases(@RequestParam(name = "size", defaultValue = "20") int size);

    @Component
    class ReconciliationClientFallback implements ReconciliationClient {
        private static final Logger log = LoggerFactory.getLogger(ReconciliationClientFallback.class);

        @Override
        public ApiResponse<Map<String, Object>> executeReconciliation(Map<String, Object> request) {
            log.warn("[ReconciliationClient Fallback] Fallback invoked for executeReconciliation");
            return ApiResponse.ok(Map.of("status", "FALLBACK_TRIGGERED"), "Fallback response");
        }

        @Override
        public ApiResponse<PagedResponse<Map<String, Object>>> listRuns(int size) {
            log.warn("[ReconciliationClient Fallback] Fallback invoked for listRuns");
            return ApiResponse.ok(PagedResponse.empty(0, size), "Fallback runs response");
        }

        @Override
        public ApiResponse<PagedResponse<Map<String, Object>>> listDiscrepancyCases(int size) {
            log.warn("[ReconciliationClient Fallback] Fallback invoked for listDiscrepancyCases");
            return ApiResponse.ok(PagedResponse.empty(0, size), "Fallback cases response");
        }
    }
}
