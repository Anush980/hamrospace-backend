package com.anush.hamrospace.auth.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.auth.dto.request.RefreshTokenRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;
import com.anush.hamrospace.auth.entity.RefreshToken;
import com.anush.hamrospace.auth.repository.RefreshTokenRepository;
import com.anush.hamrospace.auth.service.TokenLifeCycleService;
import com.anush.hamrospace.core.config.jwt.JwtProperties;
import com.anush.hamrospace.core.exception.http.UnauthorizedException;
import com.anush.hamrospace.core.security.jwt.impl.JwtService;
import com.anush.hamrospace.core.security.user.UserPrincipal;
import com.anush.hamrospace.core.security.util.ClientMetaDataUtils;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.user.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenLifeCycleServiceImpl implements TokenLifeCycleService {

    private final ClientMetaDataUtils clientMetaDataUtils;
    private final HttpServletRequest httpServletRequest;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final JwtService jwtService;

    @Override
    public AuthResponse issueTokenPair(User user, boolean rememberMe) {
        String deviceInfo = clientMetaDataUtils.extractDeviceInfo(httpServletRequest);
        String ipAddress = clientMetaDataUtils.extractIpAddress(httpServletRequest);
        String userAgent = clientMetaDataUtils.extractUserAgent(httpServletRequest);
        Instant now = Instant.now();

        refreshTokenRepository.revokeActiveTokensByUserAndDevice(user, deviceInfo, now);

        UserPrincipal userPrincipal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal, rememberMe);

        Duration expirationBuffer = rememberMe ? jwtProperties.getRefreshExpiration()
                : jwtProperties.getShortRefreshExpiration();
        Instant expiryDate = Instant.now().plus(expirationBuffer);

        RefreshToken refreshTokenObject = new RefreshToken();
        refreshTokenObject.setUser(user);
        refreshTokenObject.setToken(hashToken(refreshToken));
        refreshTokenObject.setDeviceInfo(deviceInfo);
        refreshTokenObject.setIpAddress(ipAddress);
        refreshTokenObject.setUserAgent(userAgent);
        refreshTokenObject.setExpiryDate(expiryDate);
        refreshTokenObject.setRememberMe(rememberMe);

        refreshTokenRepository.save(refreshTokenObject);

        String fullName = (user.getProfile() != null) ? user.getProfile().getFullName() : null;

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.isEmailVerified(),
                fullName,
                user.getEmail(),
                user.getUserRole().name());
    }

@Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
       
        RefreshToken savedToken = refreshTokenRepository.findByToken(hashToken(request.refreshToken()))
                .orElseThrow(() -> new UnauthorizedException("Refresh token expired or invalid"));

        
        if (savedToken.isExpired() || savedToken.isRevoked() || savedToken.isDeleted()) {
            throw new UnauthorizedException("Refresh token expired or invalid");
        }

        User user = savedToken.getUser();

        savedToken.setRevoked(true);
        savedToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(savedToken);

        return issueTokenPair(user, savedToken.isRememberMe());
    }

    @Override
    public void logout(RefreshTokenRequest request) {

        User user = SecurityUtil.getCurrentUser().getUser();
        String deviceInfo = clientMetaDataUtils.extractDeviceInfo(httpServletRequest);
        Instant now = Instant.now();
        refreshTokenRepository.revokeActiveTokensByUserAndDevice(user, deviceInfo, now);
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
