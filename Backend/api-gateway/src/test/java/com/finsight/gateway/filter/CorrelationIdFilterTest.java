package com.finsight.gateway.filter;

import com.finsight.common.constant.FinSightHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    @Test
    void testFilterGeneratesCorrelationIdWhenMissing() {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(argThat(mutatedExchange -> {
            String corrId = mutatedExchange.getRequest().getHeaders().getFirst(FinSightHeaders.CORRELATION_ID);
            return corrId != null && !corrId.isEmpty();
        }));
    }

    @Test
    void testFilterPreservesExistingCorrelationId() {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        String existingCorrId = "custom-test-correlation-id-12345";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users/me")
                .header(FinSightHeaders.CORRELATION_ID, existingCorrId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(argThat(mutatedExchange -> {
            String corrId = mutatedExchange.getRequest().getHeaders().getFirst(FinSightHeaders.CORRELATION_ID);
            return existingCorrId.equals(corrId);
        }));
    }
}
