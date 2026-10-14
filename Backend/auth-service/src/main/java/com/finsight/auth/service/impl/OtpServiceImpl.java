package com.finsight.auth.service.impl;

import com.finsight.auth.dto.request.ForgotPasswordRequest;
import com.finsight.auth.dto.request.ResetPasswordRequest;
import com.finsight.auth.dto.request.VerifyOtpRequest;
import com.finsight.auth.dto.response.OtpResponse;
import com.finsight.auth.entity.PasswordResetOtp;
import com.finsight.auth.entity.User;
import com.finsight.auth.repository.PasswordResetOtpRepository;
import com.finsight.auth.repository.UserRepository;
import com.finsight.auth.service.OtpService;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.common.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final Duration OTP_VALIDITY = Duration.ofMinutes(5);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OtpResponse generateAndSendOtp(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String otpCode = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plus(OTP_VALIDITY);

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .email(user.getEmail())
                .otpCode(otpCode)
                .deliveryChannel(request.getChannel() != null ? request.getChannel().toUpperCase() : "EMAIL")
                .expiresAt(expiresAt)
                .verified(false)
                .used(false)
                .build();

        otpRepository.save(otp);

        // Simulated high-security multi-channel notification dispatcher (Email / SMS)
        log.info("[OTP Service] >>> Secure OTP Generated for [{}]: Code='{}' | Channel={} | ExpiresAt={} <<<",
                user.getEmail(), otpCode, otp.getDeliveryChannel(), expiresAt);

        return OtpResponse.builder()
                .email(user.getEmail())
                .message("A 6-digit verification OTP has been dispatched to your " + otp.getDeliveryChannel().toLowerCase() + ". Code expires in 5 minutes.")
                .deliveryChannel(otp.getDeliveryChannel())
                .expiresAt(expiresAt)
                .verified(false)
                .build();
    }

    @Override
    @Transactional
    public OtpResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getOtpCode().trim();

        PasswordResetOtp otp = otpRepository.findTopByEmailAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(email, code)
                .orElseThrow(() -> new InvalidRequestException("Invalid or expired OTP verification code"));

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRequestException("OTP code has expired. Please request a new verification code.");
        }

        otp.setVerified(true);
        otpRepository.save(otp);

        log.info("[OTP Service] OTP successfully verified for email: {}", email);

        return OtpResponse.builder()
                .email(email)
                .message("OTP verified successfully. You may now proceed to reset your password.")
                .deliveryChannel(otp.getDeliveryChannel())
                .expiresAt(otp.getExpiresAt())
                .verified(true)
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("New password and confirmation password do not match");
        }

        String email = request.getEmail().trim().toLowerCase();
        String code = request.getOtpCode().trim();

        PasswordResetOtp otp = otpRepository.findTopByEmailAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(email, code)
                .orElseThrow(() -> new InvalidRequestException("Invalid or unverified OTP code"));

        if (!otp.isVerified()) {
            throw new InvalidRequestException("OTP code must be verified prior to resetting password");
        }

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRequestException("OTP session has expired. Please request a new code.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otp.setUsed(true);
        otpRepository.save(otp);

        log.info("[OTP Service] Password successfully reset for user: {}", email);
    }
}
