package com.anush.hamrospace.auth.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        boolean emailVerified,
        String fullName,
        String email,
        String role
    ) {}
