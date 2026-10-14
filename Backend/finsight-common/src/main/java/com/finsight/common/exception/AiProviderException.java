package com.finsight.common.exception;

import org.springframework.http.HttpStatus;

public class AiProviderException extends BaseFinSightException {
    public AiProviderException(String message) {
        super(message, "AI_PROVIDER_ERROR", HttpStatus.BAD_GATEWAY);
    }

    public AiProviderException(String message, Throwable cause) {
        super(message, cause, "AI_PROVIDER_ERROR", HttpStatus.BAD_GATEWAY);
    }
}
