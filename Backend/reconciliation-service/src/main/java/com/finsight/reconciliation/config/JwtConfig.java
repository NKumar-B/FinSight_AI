package com.finsight.reconciliation.config;

import com.finsight.common.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${finsight.jwt.secret:" + JwtUtils.DEFAULT_SECRET + "}")
    private String jwtSecret;

    @Value("${finsight.jwt.expiration-ms:" + JwtUtils.DEFAULT_EXPIRATION_MS + "}")
    private long expirationMs;

    @Value("${finsight.jwt.refresh-expiration-ms:" + JwtUtils.DEFAULT_REFRESH_EXPIRATION_MS + "}")
    private long refreshExpirationMs;

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(jwtSecret, expirationMs, refreshExpirationMs);
    }
}
