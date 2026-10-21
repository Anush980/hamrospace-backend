package com.anush.hamrospace.core.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    
    String generateAccessToken(UserDetails user);

    String generateRefreshToken(UserDetails user ,Boolean rememberMe);

    boolean isRefreshTokenValid(String token,UserDetails user);

    boolean isAccessTokenValid(String token,UserDetails user);

    String extractUsername(String token);
}
