package com.finsight.auth.service.impl;

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
import com.finsight.auth.service.AuthService;
import com.finsight.common.context.CorrelationIdContext;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.enums.RoleEnum;
import com.finsight.common.exception.ConflictException;
import com.finsight.common.exception.ForbiddenException;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.common.exception.UnauthorizedException;
import com.finsight.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("[AuthService] Registering user: username={} email={}", request.getUsername(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already in use: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                String normalized = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                Role role = roleRepository.findByName(normalized)
                        .orElseGet(() -> roleRepository.save(Role.builder()
                                .name(normalized)
                                .description("Role " + normalized)
                                .build()));
                roles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByName(RoleEnum.ROLE_USER.name())
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(RoleEnum.ROLE_USER.name())
                            .description(RoleEnum.ROLE_USER.getDescription())
                            .build()));
            roles.add(defaultRole);
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .active(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        // Async audit record dispatch
        sendAuditSafe("USER_REGISTER", "USER", savedUser.getId().toString(), "SUCCESS",
                Map.of("username", savedUser.getUsername(), "roles", roles.stream().map(Role::getName).toList()));

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("[AuthService] Processing login attempt for: {}", request.getUsernameOrEmail());

        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("[AuthService] Failed login attempt for user: {}", request.getUsernameOrEmail());
            sendAuditSafe("USER_LOGIN_FAILED", "USER", user.getId().toString(), "FAILURE",
                    Map.of("usernameOrEmail", request.getUsernameOrEmail()));
            throw new UnauthorizedException("Invalid username or password");
        }

        if (!user.isActive()) {
            log.warn("[AuthService] Inactive user login attempt: {}", user.getUsername());
            throw new ForbiddenException("Account is disabled. Please contact system administrator.");
        }

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getEmail(), roleNames);
        String rawRefreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        // Revoke previous tokens and save new refresh token
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(rawRefreshToken)
                .expiryDate(Instant.now().plusMillis(JwtUtils.DEFAULT_REFRESH_EXPIRATION_MS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        sendAuditSafe("USER_LOGIN", "USER", user.getId().toString(), "SUCCESS",
                Map.of("username", user.getUsername(), "roles", roleNames));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(JwtUtils.DEFAULT_EXPIRATION_MS)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String tokenStr = request.getRefreshToken();
        if (!jwtUtils.validateToken(tokenStr)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found or revoked"));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token is expired or has been revoked");
        }

        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new ForbiddenException("Account is disabled");
        }

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String newAccessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getEmail(), roleNames);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(tokenStr)
                .tokenType("Bearer")
                .expiresInMs(JwtUtils.DEFAULT_EXPIRATION_MS)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return userMapper.toResponse(user);
    }

    private void sendAuditSafe(String action, String entityType, String entityId, String outcome, Map<String, Object> metadata) {
        try {
            AuditEventDto audit = AuditEventDto.builder()
                    .actorId(entityId)
                    .actorType("USER")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .correlationId(CorrelationIdContext.get())
                    .outcome(outcome)
                    .metadata(metadata)
                    .timestamp(Instant.now())
                    .build();
            auditClient.recordAuditEvent(audit);
        } catch (Exception e) {
            log.warn("[AuthService] Could not propagate audit event synchronously: {}", e.getMessage());
        }
    }
}
