package com.finsight.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseFinSightException {
    public ConflictException(String message) {
        super(message, "DUPLICATE_RESOURCE_CONFLICT", HttpStatus.CONFLICT);
    }

    public ConflictException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.CONFLICT);
    }
}
