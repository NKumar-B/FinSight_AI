package com.finsight.auth.controller;

import com.finsight.auth.dto.request.*;
import com.finsight.auth.dto.response.AuthResponse;
import com.finsight.auth.dto.response.NavigationMenuResponse;
import com.finsight.auth.dto.response.OtpResponse;
import com.finsight.auth.dto.response.UserResponse;
import com.finsight.auth.service.AuthService;
import com.finsight.auth.service.OtpService;
import com.finsight.auth.service.RoleMenuService;
import com.finsight.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, OTP verification, password resets, and dynamic role-based navigation")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final RoleMenuService roleMenuService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account", description = "Creates a new user with hashed credentials and assigned roles.")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.created(response, "User registered successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT", description = "Validates credentials and returns JWT access and refresh tokens.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh expired access token", description = "Uses a valid refresh token to issue a renewed access token.")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Token refreshed successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", description = "Retrieves user details and roles for the bearer token subject.")
    public ResponseEntity<ApiResponse<UserResponse>> me(Principal principal) {
        UserResponse response = authService.getCurrentUserProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok(response, "Profile retrieved successfully"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset OTP", description = "Generates and sends a 6-digit OTP code to the user's registered email or phone.")
    public ResponseEntity<ApiResponse<OtpResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        OtpResponse response = otpService.generateAndSendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(response, response.getMessage()));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP code", description = "Verifies the 6-digit OTP code prior to password reset.")
    public ResponseEntity<ApiResponse<OtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        OtpResponse response = otpService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(response, response.getMessage()));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset account password", description = "Sets a new account password after successful OTP verification.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        otpService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Password reset successfully. You may now log in with your new credentials."));
    }

    @GetMapping("/menu")
    @Operation(summary = "Get role-based dynamic navigation menu", description = "Returns the allowed navigation tree, routes, and feature permissions according to the authenticated user's roles.")
    public ResponseEntity<ApiResponse<NavigationMenuResponse>> getNavigationMenu(Principal principal) {
        NavigationMenuResponse response = roleMenuService.getMenuForUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok(response, "Role-based navigation menu retrieved successfully"));
    }
}
