package com.finsight.common.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseFinSightException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    protected BaseFinSightException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected BaseFinSightException(String message, Throwable cause, String errorCode, HttpStatus httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
