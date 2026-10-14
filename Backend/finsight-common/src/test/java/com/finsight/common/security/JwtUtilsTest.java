package com.finsight.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        Long userId = 101L;
        String username = "analyst1";
        String email = "analyst@finsight.ai";
        Set<String> roles = Set.of("ROLE_FINANCIAL_ANALYST");

        String token = jwtUtils.generateAccessToken(userId, username, email, roles);
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));

        assertEquals(username, jwtUtils.extractUsername(token));
        assertEquals(userId, jwtUtils.extractUserId(token));
        assertEquals(email, jwtUtils.extractEmail(token));

        List<String> extractedRoles = jwtUtils.extractRoles(token);
        assertEquals(1, extractedRoles.size());
        assertEquals("ROLE_FINANCIAL_ANALYST", extractedRoles.get(0));
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = jwtUtils.generateRefreshToken(102L, "admin_user");
        assertNotNull(refreshToken);
        assertTrue(jwtUtils.validateToken(refreshToken));
        assertEquals("admin_user", jwtUtils.extractUsername(refreshToken));
    }
}
