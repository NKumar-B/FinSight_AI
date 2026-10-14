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

@FeignClient(name = "fraud-detection-service", fallback = FraudDetectionClient.FraudDetectionClientFallback.class)
public interface FraudDetectionClient {

    @PostMapping("/api/v1/fraud/evaluate")
    ApiResponse<Map<String, Object>> evaluateTransaction(@RequestBody Map<String, Object> request);

    @GetMapping("/api/v1/fraud/alerts")
    ApiResponse<PagedResponse<Map<String, Object>>> listAlerts(@RequestParam(name = "size", defaultValue = "20") int size);

    @GetMapping("/api/v1/fraud/alerts/stats")
    ApiResponse<Map<String, Object>> getAlertStats();

    @Component
    class FraudDetectionClientFallback implements FraudDetectionClient {
        private static final Logger log = LoggerFactory.getLogger(FraudDetectionClientFallback.class);

        @Override
        public ApiResponse<Map<String, Object>> evaluateTransaction(Map<String, Object> request) {
            log.warn("[FraudDetectionClient Fallback] Fallback invoked for evaluateTransaction");
            return ApiResponse.ok(Map.of("riskScore", 0, "severity", "LOW", "alertGenerated", false), "Fallback evaluation");
        }

        @Override
        public ApiResponse<PagedResponse<Map<String, Object>>> listAlerts(int size) {
            log.warn("[FraudDetectionClient Fallback] Fallback invoked for listAlerts");
            return ApiResponse.ok(PagedResponse.empty(0, size), "Fallback alerts");
        }

        @Override
        public ApiResponse<Map<String, Object>> getAlertStats() {
            log.warn("[FraudDetectionClient Fallback] Fallback invoked for getAlertStats");
            return ApiResponse.ok(Map.of("totalAlerts", 0), "Fallback stats");
        }
    }
}
