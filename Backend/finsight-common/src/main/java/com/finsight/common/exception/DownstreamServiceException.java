package com.finsight.common.exception;

import org.springframework.http.HttpStatus;

public class DownstreamServiceException extends BaseFinSightException {
    public DownstreamServiceException(String serviceName, String message) {
        super(String.format("Downstream dependency [%s] failure: %s", serviceName, message),
                "DOWNSTREAM_SERVICE_UNAVAILABLE", HttpStatus.BAD_GATEWAY);
    }
}
