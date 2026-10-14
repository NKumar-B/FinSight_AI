package com.finsight.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.auth.dto.request.*;
import com.finsight.auth.dto.response.AuthResponse;
import com.finsight.auth.dto.response.NavigationMenuResponse;
import com.finsight.auth.dto.response.OtpResponse;
import com.finsight.auth.dto.response.UserResponse;
import com.finsight.auth.security.JwtAuthenticationFilter;
import com.finsight.auth.service.AuthService;
import com.finsight.auth.service.OtpService;
import com.finsight.auth.service.RoleMenuService;
import com.finsight.common.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private RoleMenuService roleMenuService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void testRegisterValidPayloadReturnsCreated() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("analyst1")
                .email("analyst1@finsight.ai")
                .password("SecurePass1")
                .fullName("Fin Analyst")
                .roles(Set.of("ROLE_FINANCIAL_ANALYST"))
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("analyst1")
                .email("analyst1@finsight.ai")
                .fullName("Fin Analyst")
                .active(true)
                .roles(Set.of("ROLE_FINANCIAL_ANALYST"))
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("analyst1"))
                .andExpect(jsonPath("$.data.email").value("analyst1@finsight.ai"));
    }

    @Test
    void testRegisterInvalidPayloadReturnsBadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("")
                .email("invalid-email")
                .password("weak")
                .fullName("")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void testLoginSuccessReturnsToken() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("analyst1")
                .password("SecurePass1")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mocked.jwt.token")
                .refreshToken("mocked.refresh.token")
                .tokenType("Bearer")
                .expiresInMs(86400000)
                .user(UserResponse.builder().id(1L).username("analyst1").build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("mocked.refresh.token"));
    }

    @Test
    void testForgotPasswordGeneratesOtp() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("analyst1@finsight.ai")
                .channel("EMAIL")
                .build();

        OtpResponse response = OtpResponse.builder()
                .email("analyst1@finsight.ai")
                .deliveryChannel("EMAIL")
                .message("OTP dispatched successfully")
                .expiresAt(Instant.now().plusSeconds(300))
                .verified(false)
                .build();

        when(otpService.generateAndSendOtp(any(ForgotPasswordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deliveryChannel").value("EMAIL"));
    }

    @Test
    void testVerifyOtpSuccess() throws Exception {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email("analyst1@finsight.ai")
                .otpCode("123456")
                .build();

        OtpResponse response = OtpResponse.builder()
                .email("analyst1@finsight.ai")
                .deliveryChannel("EMAIL")
                .message("OTP verified")
                .verified(true)
                .build();

        when(otpService.verifyOtp(any(VerifyOtpRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verified").value(true));
    }

    @Test
    void testGetNavigationMenuReturnsDynamicTree() throws Exception {
        Principal principal = () -> "analyst1";
        NavigationMenuResponse menuResponse = NavigationMenuResponse.builder()
                .username("analyst1")
                .fullName("Fin Analyst")
                .roles(Set.of("ROLE_FINANCIAL_ANALYST"))
                .menuItems(Collections.emptyList())
                .permissions(Collections.emptyList())
                .build();

        when(roleMenuService.getMenuForUser("analyst1")).thenReturn(menuResponse);

        mockMvc.perform(get("/api/v1/auth/menu").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("analyst1"));
    }
}
