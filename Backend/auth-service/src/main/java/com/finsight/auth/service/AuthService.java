package com.finsight.auth.service;

import com.finsight.auth.dto.request.LoginRequest;
import com.finsight.auth.dto.request.RegisterRequest;
import com.finsight.auth.dto.request.TokenRefreshRequest;
import com.finsight.auth.dto.response.AuthResponse;
import com.finsight.auth.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(TokenRefreshRequest request);
    UserResponse getCurrentUserProfile(String username);
}
