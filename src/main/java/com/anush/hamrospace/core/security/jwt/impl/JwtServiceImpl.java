package com.anush.hamrospace.core.security.jwt.impl;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.anush.hamrospace.core.config.jwt.JwtProperties;
import com.anush.hamrospace.core.security.jwt.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    @Override
    public String generateAccessToken(UserDetails user) {
        return buildToken(user, jwtProperties.getAccessExpiration().toMillis(), "ACCESS", getAccessKey());
    }

    @Override
    public String generateRefreshToken(UserDetails user, Boolean rememberMe) {
        long expirationMillis = rememberMe
                ? jwtProperties.getRefreshExpiration().toMillis()
                : jwtProperties.getShortRefreshExpiration().toMillis();

        return buildToken(user, expirationMillis, "REFRESH", getRefreshKey());
    }

    @Override
    public boolean isRefreshTokenValid(String token, UserDetails user) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getRefreshKey())
                    .build().parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String type = claims.get("type", String.class);

            return username != null
                    && username.equals(user.getUsername())
                    && "REFRESH".equals(type)
                    && !claims.getExpiration().before(new Date());

        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Refresh Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean isAccessTokenValid(String token, UserDetails user) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getAccessKey())
                    .build().parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String type = claims.get("type", String.class);

            return username != null
                    && username.equals(user.getUsername())
                    && "ACCESS".equals(type)
                    && !claims.getExpiration().before(new Date());

        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Access Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public String extractUsername(String token) {
        try {
            return Jwts.parser().verifyWith(getAccessKey()).build().parseSignedClaims(token).getPayload().getSubject();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Could not extract username from token: " + ex.getMessage());
            return null;
        }
    }

    private SecretKey getAccessKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessKey()));
    }

    private SecretKey getRefreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getRefreshKey()));
    }

    private String buildToken(UserDetails user, long expirationMs, String type, SecretKey key) {
        return Jwts.builder()
                .subject(user.getUsername())
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

}
