package com.anush.hamrospace.auth.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.auth.entity.EmailVerificationToken;
import com.anush.hamrospace.auth.event.ResendEmailVerificationEvent;
import com.anush.hamrospace.auth.repository.EmailVerificationTokenRepository;
import com.anush.hamrospace.auth.service.EmailVerificationService;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final int TOKEN_EXPIRY_MINUTE=15;
    private final Duration TOKEN_EXPIRY_DURATION = Duration.ofMinutes(TOKEN_EXPIRY_MINUTE);

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
    public String validateVerificationToken(String token) {
        String hashedToken = hashToken(token);

        EmailVerificationToken stored = emailVerificationTokenRepository.findByToken(hashedToken).orElseThrow(()-> new BadRequestException("Invalid or expired token"));

        if (stored.isDeleted() || stored.isExpired() || stored.isUsed()) {
            throw new BadRequestException("Invalid or expired token");
        }
        return stored.getUser().getEmail();
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if(user.isEmailVerified()){
                    throw new BadRequestException("Email is already verified");
                }

                String token = generateVerificationToken(email);
                Instant expiresAt = Instant.now().plus(TOKEN_EXPIRY_DURATION);

                eventPublisher.publishEvent(new ResendEmailVerificationEvent(user.getId(), email, user.getProfile().getFullName(), token,expiresAt ));
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
            byte[] hash = md.digest(token.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

}
