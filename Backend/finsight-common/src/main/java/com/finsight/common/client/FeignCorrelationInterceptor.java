package com.finsight.common.client;

import com.finsight.common.constant.FinSightHeaders;
import com.finsight.common.context.CorrelationIdContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignCorrelationInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String correlationId = CorrelationIdContext.get();
        if (correlationId != null && !template.headers().containsKey(FinSightHeaders.CORRELATION_ID)) {
            template.header(FinSightHeaders.CORRELATION_ID, correlationId);
        }
    }
}
