package com.anush.hamrospace.auth.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.auth.dto.request.ChangePasswordRequest;
import com.anush.hamrospace.auth.dto.request.LoginRequest;
import com.anush.hamrospace.auth.dto.request.RefreshTokenRequest;
import com.anush.hamrospace.auth.dto.request.RegisterRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;
import com.anush.hamrospace.auth.event.PasswordChangedEvent;
import com.anush.hamrospace.auth.event.UserRegisteredEvent;
import com.anush.hamrospace.auth.service.AuthService;
import com.anush.hamrospace.auth.service.EmailVerificationService;
import com.anush.hamrospace.auth.service.TokenLifeCycleService;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ConflictException;
import com.anush.hamrospace.core.exception.http.UnauthorizedException;
import com.anush.hamrospace.core.security.util.ClientMetaDataUtils;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenLifeCycleService tokenLifeCycleService;
    private final ApplicationEventPublisher eventPublisher;
    private final ClientMetaDataUtils clientMetaDataUtils;
    private final HttpServletRequest httpServletRequest;
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

        if (user.getBlockedAt() != null) {
            throw new UnauthorizedException("Your account has been blocked");
        }

        boolean rememberMe = Boolean.TRUE.equals(loginRequest.rememberMe());
        user.setLastLoginAt(Instant.now());

        return tokenLifeCycleService.issueTokenPair(user, rememberMe);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

        UUID userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be same as the old password");
        }

        String deviceInfo = clientMetaDataUtils.extractDeviceInfo(httpServletRequest);
        String ipAddress = clientMetaDataUtils.extractIpAddress(httpServletRequest);
        String userAgent = clientMetaDataUtils.extractUserAgent(httpServletRequest);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        eventPublisher.publishEvent(new PasswordChangedEvent(userId, user.getEmail(), user.getProfile().getFullName(),
                deviceInfo, ipAddress, userAgent));
    }

    @Override
    public void logout(RefreshTokenRequest refreshTokenRequest) {
        tokenLifeCycleService.logout(refreshTokenRequest);
    }


    // @Override
    // public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
    //     User user = userRepository.findByEmail(forgotPasswordRequest.email()).orElse(null);
    //     if (user == null)
    //         return;
    //     Instant now = Instant.now();
    //     passwordResetTokenRepository.invalidateAllByUser(user, now);


    //     PasswordResetToken resetToken = new PasswordResetToken();
    //     resetToken.setUser(user);
    //     resetToken.setToken(hashedToken);
    //     resetToken.setExpiryDate(Instant.now().plus(Duration.ofMinutes(TOKEN_EXPIRY_MINUTE)));

    //     passwordResetTokenRepository.save(resetToken);

    //     eventPublisher.publishEvent(new ForgotPasswordEvent(user.getId(), user.getEmail(),
    //             user.getProfile().getFullName(), token, TOKEN_EXPIRY_MINUTE));
    // }

    // @Override
    // public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
    //     String hashedToken = hashToken(resetPasswordRequest.token());

    //     PasswordResetToken token = passwordResetTokenRepository.findByToken(hashedToken)
    //             .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

    //     if (token.isDeleted() || token.isExpired() || token.isUsed()) {
    //         throw new BadRequestException("Invalid or expired token");
    //     }

    //     User user = token.getUser();

    //     if (passwordEncoder.matches(resetPasswordRequest.newPassword(), user.getPassword())) {
    //         throw new BadRequestException("New password cannot be same as the old password");
    //     }
    //     user.setPassword(passwordEncoder.encode(resetPasswordRequest.newPassword()));
    //     userRepository.save(user);

    //     token.setUsedAt(Instant.now());
    //     passwordResetTokenRepository.save(token);

    //     String deviceInfo = clientMetaDataUtils.extractDeviceInfo(httpServletRequest);
    //     String ipAddress = clientMetaDataUtils.extractIpAddress(httpServletRequest);
    //     String userAgent = clientMetaDataUtils.extractUserAgent(httpServletRequest);

    //     eventPublisher.publishEvent(new PasswordChangedEvent(user.getId(), user.getEmail(),
    //             user.getProfile().getFullName(), deviceInfo, ipAddress, userAgent));
    // }

}
