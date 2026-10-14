package com.finsight.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends BaseFinSightException {
    public InvalidRequestException(String message) {
        super(message, "INVALID_REQUEST", HttpStatus.BAD_REQUEST);
    }

    public InvalidRequestException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
}
