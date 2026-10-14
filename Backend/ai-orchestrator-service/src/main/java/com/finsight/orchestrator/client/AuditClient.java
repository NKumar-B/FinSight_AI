package com.finsight.orchestrator.client;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.AuditEventDto;
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

@FeignClient(name = "audit-reporting-service", fallback = AuditClient.AuditClientFallback.class)
public interface AuditClient {

    @PostMapping("/api/v1/audit/events")
    ApiResponse<Void> recordAuditEvent(@RequestBody AuditEventDto auditEventDto);

    @GetMapping("/api/v1/audit/events")
    ApiResponse<PagedResponse<Map<String, Object>>> getEvents(@RequestParam(name = "size", defaultValue = "20") int size);

    @Component
    class AuditClientFallback implements AuditClient {
        private static final Logger log = LoggerFactory.getLogger(AuditClientFallback.class);

        @Override
        public ApiResponse<Void> recordAuditEvent(AuditEventDto auditEventDto) {
            log.warn("[AuditClient Fallback] Fallback for recordAuditEvent: action={}", auditEventDto.getAction());
            return ApiResponse.ok(null, "Fallback executed");
        }

        @Override
        public ApiResponse<PagedResponse<Map<String, Object>>> getEvents(int size) {
            log.warn("[AuditClient Fallback] Fallback for getEvents");
            return ApiResponse.ok(PagedResponse.empty(0, size), "Fallback audit response");
        }
    }
}
