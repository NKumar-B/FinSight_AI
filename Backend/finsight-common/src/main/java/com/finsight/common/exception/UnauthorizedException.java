package com.finsight.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseFinSightException {
    public UnauthorizedException(String message) {
        super(message, "AUTHENTICATION_REQUIRED", HttpStatus.UNAUTHORIZED);
    }
}
