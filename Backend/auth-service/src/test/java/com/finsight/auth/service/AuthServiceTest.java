package com.finsight.auth.service;

import com.finsight.auth.client.AuditClient;
import com.finsight.auth.dto.request.LoginRequest;
import com.finsight.auth.dto.request.RegisterRequest;
import com.finsight.auth.dto.request.TokenRefreshRequest;
import com.finsight.auth.dto.response.AuthResponse;
import com.finsight.auth.dto.response.UserResponse;
import com.finsight.auth.entity.RefreshToken;
import com.finsight.auth.entity.Role;
import com.finsight.auth.entity.User;
import com.finsight.auth.mapper.UserMapper;
import com.finsight.auth.repository.RefreshTokenRepository;
import com.finsight.auth.repository.RoleRepository;
import com.finsight.auth.repository.UserRepository;
import com.finsight.auth.service.impl.AuthServiceImpl;
import com.finsight.common.exception.ConflictException;
import com.finsight.common.exception.ForbiddenException;
import com.finsight.common.exception.UnauthorizedException;
import com.finsight.common.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuditClient auditClient;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("ROLE_USER").description("User role").build();
        sampleUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@finsight.ai")
                .passwordHash("hashed_secret")
                .fullName("John Doe")
                .active(true)
                .roles(Set.of(userRole))
                .build();
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@finsight.ai")
                .password("Password123")
                .fullName("John Doe")
                .build();

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@finsight.ai")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Password123")).thenReturn("hashed_secret");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(userMapper.toResponse(sampleUser)).thenReturn(UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john@finsight.ai")
                .fullName("John Doe")
                .active(true)
                .roles(Set.of("ROLE_USER"))
                .build());

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("john_doe", response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateUsernameThrowsConflict() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@finsight.ai")
                .password("Password123")
                .fullName("John Doe")
                .build();

        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("Password123")
                .build();

        when(userRepository.findByUsernameOrEmail("john_doe", "john_doe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password123", "hashed_secret")).thenReturn(true);
        when(jwtUtils.generateAccessToken(eq(1L), eq("john_doe"), eq("john@finsight.ai"), any())).thenReturn("mock-access-token");
        when(jwtUtils.generateRefreshToken(eq(1L), eq("john_doe"))).thenReturn("mock-refresh-token");
        when(userMapper.toResponse(sampleUser)).thenReturn(UserResponse.builder().id(1L).username("john_doe").build());

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testLoginInvalidPasswordThrowsUnauthorized() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("WrongPassword")
                .build();

        when(userRepository.findByUsernameOrEmail("john_doe", "john_doe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword", "hashed_secret")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testLoginInactiveUserThrowsForbidden() {
        sampleUser.setActive(false);
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("Password123")
                .build();

        when(userRepository.findByUsernameOrEmail("john_doe", "john_doe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password123", "hashed_secret")).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> authService.login(request));
    }

    @Test
    void testRefreshTokenSuccess() {
        TokenRefreshRequest request = TokenRefreshRequest.builder().refreshToken("valid-token").build();
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(sampleUser)
                .token("valid-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(jwtUtils.validateToken("valid-token")).thenReturn(true);
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(refreshToken));
        when(jwtUtils.generateAccessToken(eq(1L), eq("john_doe"), eq("john@finsight.ai"), any())).thenReturn("new-jwt-token");
        when(userMapper.toResponse(sampleUser)).thenReturn(UserResponse.builder().id(1L).username("john_doe").build());

        AuthResponse response = authService.refreshToken(request);
        assertNotNull(response);
        assertEquals("new-jwt-token", response.getAccessToken());
    }
}
