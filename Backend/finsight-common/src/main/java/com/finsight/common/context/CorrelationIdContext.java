package com.finsight.common.context;

import java.util.UUID;

public final class CorrelationIdContext {
    private static final ThreadLocal<String> CURRENT_CORRELATION_ID = new ThreadLocal<>();

    private CorrelationIdContext() {
    }

    public static String get() {
        String correlationId = CURRENT_CORRELATION_ID.get();
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            CURRENT_CORRELATION_ID.set(correlationId);
        }
        return correlationId;
    }

    public static void set(String correlationId) {
        if (correlationId != null && !correlationId.trim().isEmpty()) {
            CURRENT_CORRELATION_ID.set(correlationId);
        } else {
            CURRENT_CORRELATION_ID.set(UUID.randomUUID().toString());
        }
    }

    public static void clear() {
        CURRENT_CORRELATION_ID.remove();
    }
}
