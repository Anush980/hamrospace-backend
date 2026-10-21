package com.anush.hamrospace.auth.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.auth.entity.EmailVerificationToken;
import com.anush.hamrospace.auth.event.ResendEmailVerificationEvent;
import com.anush.hamrospace.auth.repository.EmailVerificationTokenRepository;
import com.anush.hamrospace.auth.service.EmailVerificationService;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final Duration TOKEN_EXPIRY_DURATION = Duration.ofMinutes(TOKEN_EXPIRY_MINUTES);

    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String generateVerificationToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Instant now = Instant.now();
        emailVerificationTokenRepository.invalidateAllByUser(user, now);
        
        String otp = generateOtp();

        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(hashToken(otp));
        token.setUser(user);
        token.setExpiryDate(now.plus(TOKEN_EXPIRY_DURATION));
        emailVerificationTokenRepository.save(token);
        
        return otp;
    }

    @Override
    @Transactional(readOnly = true)
    public String validateVerificationToken(String token) {
        EmailVerificationToken stored = getValidToken(token);
        return stored.getUser().getEmail();
    }

    @Override
    public void resendVerificationEmail() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        String otp = generateVerificationToken(email);
        Instant expiresAt = Instant.now().plus(TOKEN_EXPIRY_DURATION);

        eventPublisher.publishEvent(new ResendEmailVerificationEvent(
                user.getId(), email, user.getProfile().getFullName(), otp, expiresAt));
    }

    @Override
    public void verifyEmail(String token) {
        EmailVerificationToken storedToken = getValidToken(token);
        User user = storedToken.getUser();

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(Instant.now());
            userRepository.save(user);
        }

        storedToken.setUsedAt(Instant.now());
        emailVerificationTokenRepository.save(storedToken);
    }

    private EmailVerificationToken getValidToken(String rawToken) {
        String hashedToken = hashToken(rawToken);

        EmailVerificationToken stored = emailVerificationTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

        if (stored.isDeleted() || stored.isExpired() || stored.isUsed()) {
            throw new BadRequestException("Invalid or expired token");
        }

        return stored;
    }

    private String generateOtp() {
        int otpInt = secureRandom.nextInt(1_000_000);
        return String.format("%06d", otpInt);
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Standardize charset explicitly
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}