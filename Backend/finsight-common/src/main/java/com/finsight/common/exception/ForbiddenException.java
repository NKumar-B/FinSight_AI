package com.finsight.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseFinSightException {
    public ForbiddenException(String message) {
        super(message, "ACCESS_DENIED", HttpStatus.FORBIDDEN);
    }
}
