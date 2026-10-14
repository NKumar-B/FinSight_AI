package com.finsight.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {

    private String email;
    private String message;
    private String deliveryChannel;
    private Instant expiresAt;
    private boolean verified;
}
