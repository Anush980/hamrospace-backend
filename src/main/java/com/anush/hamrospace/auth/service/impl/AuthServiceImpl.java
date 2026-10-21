package com.anush.hamrospace.auth.service.impl;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.auth.dto.request.LoginRequest;
import com.anush.hamrospace.auth.dto.request.RefreshTokenRequest;
import com.anush.hamrospace.auth.dto.request.RegisterRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;
import com.anush.hamrospace.auth.event.UserRegisteredEvent;
import com.anush.hamrospace.auth.service.AuthService;
import com.anush.hamrospace.auth.service.EmailVerificationService;
import com.anush.hamrospace.auth.service.TokenLifeCycleService;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ConflictException;
import com.anush.hamrospace.core.exception.http.UnauthorizedException;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserStatus;
import com.anush.hamrospace.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenLifeCycleService tokenLifeCycleService;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailVerificationService emailVerificationService;

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {

        if (!registerRequest.termsAccepted()) {
            throw new BadRequestException("You must accept the terms and conditions");
        }

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setEmail(registerRequest.email());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setTermsAcceptedAt(Instant.now());
        

        Profile profile = new Profile();
        profile.setFullName(registerRequest.fullName());
        user.setProfile(profile);

        User savedUser = userRepository.save(user);

        String token = emailVerificationService.generateVerificationToken(registerRequest.email());

        // welcome email and verification otp event puslisher
        eventPublisher.publishEvent(
                new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail(), savedUser.getProfile().getFullName(),
                        token));

        // we keep the short lived refresh token during registration
        return tokenLifeCycleService.issueTokenPair(savedUser, false);
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {

        User user = userRepository.findByEmailWithProfile(loginRequest.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Your account has been temporarily suspended");
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new UnauthorizedException("Your account has been blocked");
        }

        boolean rememberMe = Boolean.TRUE.equals(loginRequest.rememberMe());
        user.setLastLoginAt(Instant.now());

        return tokenLifeCycleService.issueTokenPair(user, rememberMe);
    }

    @Override
    public void logout(RefreshTokenRequest refreshTokenRequest) {
        tokenLifeCycleService.logout(refreshTokenRequest);
    }

}
