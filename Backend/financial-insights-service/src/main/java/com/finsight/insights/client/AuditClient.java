package com.finsight.insights.client;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.AuditEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "audit-reporting-service", fallback = AuditClient.AuditClientFallback.class)
public interface AuditClient {

    @PostMapping("/api/v1/audit/events")
    ApiResponse<Void> recordAuditEvent(@RequestBody AuditEventDto auditEventDto);

    @Component
    class AuditClientFallback implements AuditClient {
        private static final Logger log = LoggerFactory.getLogger(AuditClientFallback.class);

        @Override
        public ApiResponse<Void> recordAuditEvent(AuditEventDto auditEventDto) {
            log.warn("[AuditClient Fallback] Audit service unavailable. Event logged locally: action={} entityType={} entityId={}",
                    auditEventDto.getAction(), auditEventDto.getEntityType(), auditEventDto.getEntityId());
            return ApiResponse.ok(null, "Audit event fallback executed");
        }
    }
}
