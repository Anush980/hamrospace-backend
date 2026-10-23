package com.anush.hamrospace.auth.service.impl;

import com.anush.hamrospace.auth.repository.PasswordResetTokenRepository;
import com.anush.hamrospace.auth.repository.RefreshTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.auth.dto.request.ChangePasswordRequest;
import com.anush.hamrospace.auth.dto.request.ForgotPasswordRequest;
import com.anush.hamrospace.auth.dto.request.ResetPasswordRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;
import com.anush.hamrospace.auth.entity.PasswordResetToken;
import com.anush.hamrospace.auth.event.ForgotPasswordEvent;
import com.anush.hamrospace.auth.event.PasswordChangedEvent;
import com.anush.hamrospace.auth.service.PasswordResetService;
import com.anush.hamrospace.auth.service.TokenLifeCycleService;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.ClientMetaDataUtils;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenLifeCycleService tokenLifeCycleService;
    private final SecureRandom secureRandom;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientMetaDataUtils clientMetaDataUtils;
    private final HttpServletRequest httpServletRequest;
    private final ApplicationEventPublisher eventPublisher;
    private final int TOKEN_EXPIRY_MINUTE = 15;
    private final Duration TOKEN_EXPIRY_DURATION = Duration.ofMinutes(TOKEN_EXPIRY_MINUTE);

    @Override
    public AuthResponse changePassword(ChangePasswordRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be same as the old password");
        }

        String deviceInfo = clientMetaDataUtils.extractDeviceInfo(httpServletRequest);
        String ipAddress = clientMetaDataUtils.extractIpAddress(httpServletRequest);
        String userAgent = clientMetaDataUtils.extractUserAgent(httpServletRequest);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllActiveTokensByUser(user, Instant.now());

        eventPublisher.publishEvent(new PasswordChangedEvent(userId, user.getEmail(), user.getProfile().getFullName(),
                deviceInfo, ipAddress, userAgent));

        return tokenLifeCycleService.issueTokenPair(user, false);

    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Instant now = Instant.now();
        passwordResetTokenRepository.invalidateAllByUser(user, now);
        String otp = generateOtp();

        Instant expiresAt = now.plus(TOKEN_EXPIRY_DURATION);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(hashToken(otp));
        token.setUser(user);
        token.setExpiryDate(expiresAt);

        passwordResetTokenRepository.save(token);

        eventPublisher.publishEvent(new ForgotPasswordEvent(user.getId(), user.getEmail(),
                user.getProfile().getFullName(), otp, expiresAt));
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String hashedToken = hashToken(request.token());

        PasswordResetToken stored = passwordResetTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

        if (stored.isDeleted() || stored.isExpired() || stored.isUsed()) {
            throw new BadRequestException("Invalid or expired token");
        }

        Instant now = Instant.now();
        stored.setUser(user);
        stored.setUsedAt(now);
        passwordResetTokenRepository.save(stored);

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be same as the old password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        String deviceInfo = clientMetaDataUtils.extractDeviceInfo(httpServletRequest);
        String ipAddress = clientMetaDataUtils.extractIpAddress(httpServletRequest);
        String userAgent = clientMetaDataUtils.extractUserAgent(httpServletRequest);

        eventPublisher
                .publishEvent(new PasswordChangedEvent(user.getId(), user.getEmail(), user.getProfile().getFullName(),
                        deviceInfo, ipAddress, userAgent));
    }

    private String generateOtp() {

        // Generates a random number between 0 and 999999
        int otpInt = secureRandom.nextInt(1_000_000);
        // Formats it with leading zeros so it's always strictly 6 characters
        return String.format("%06d", otpInt);
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

}
