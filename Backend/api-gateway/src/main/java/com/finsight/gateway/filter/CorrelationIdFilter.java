package com.finsight.gateway.filter;

import com.finsight.common.constant.FinSightHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(FinSightHeaders.CORRELATION_ID);

        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String effectiveCorrelationId = correlationId;
        log.info("[Gateway] Incoming Request: method={} path={} correlationId={}",
                request.getMethod(), request.getURI().getPath(), effectiveCorrelationId);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(FinSightHeaders.CORRELATION_ID, effectiveCorrelationId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        mutatedExchange.getResponse().getHeaders().add(FinSightHeaders.CORRELATION_ID, effectiveCorrelationId);

        return chain.filter(mutatedExchange)
                .then(Mono.fromRunnable(() -> {
                    log.info("[Gateway] Outgoing Response: status={} correlationId={}",
                            mutatedExchange.getResponse().getStatusCode(), effectiveCorrelationId);
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
