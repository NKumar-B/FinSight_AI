package com.finsight.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.common.constant.FinSightHeaders;
import com.finsight.common.dto.ErrorResponse;
import com.finsight.common.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGatewayFilterFactory.class);

    private final JwtUtils jwtUtils = new JwtUtils();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    public static class Config {
        // Configuration properties if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Allow public auth endpoints
            if (path.contains("/auth/register") || path.contains("/auth/login") || path.contains("/auth/refresh")
                    || path.contains("/auth/forgot-password") || path.contains("/auth/verify-otp") || path.contains("/auth/reset-password")
                    || path.contains("/actuator")) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(FinSightHeaders.BEARER_PREFIX)) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(FinSightHeaders.BEARER_PREFIX.length());
            if (!jwtUtils.validateToken(token)) {
                return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            }

            Long userId = jwtUtils.extractUserId(token);
            String email = jwtUtils.extractEmail(token);
            List<String> roles = jwtUtils.extractRoles(token);

            ServerHttpRequest.Builder builder = request.mutate();
            if (userId != null) {
                builder.header(FinSightHeaders.USER_ID, String.valueOf(userId));
            }
            if (email != null) {
                builder.header(FinSightHeaders.USER_EMAIL, email);
            }
            if (roles != null && !roles.isEmpty()) {
                builder.header(FinSightHeaders.USER_ROLES, String.join(",", roles));
            }

            return chain.filter(exchange.mutate().request(builder.build()).build());
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String correlationId = exchange.getRequest().getHeaders().getFirst(FinSightHeaders.CORRELATION_ID);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(httpStatus.value())
                .errorCode("UNAUTHORIZED")
                .message(message)
                .path(exchange.getRequest().getURI().getPath())
                .correlationId(correlationId)
                .build();

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            bytes = ("{\"status\":401,\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
