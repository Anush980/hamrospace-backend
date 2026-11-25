package com.anush.hamrospace.admin.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;

public record AdminUserResponse(
        UUID id,
        String email,
        UserRole role,
        UserStatus status,
        boolean emailVerified,
        String fullName,
        String blockedReason,
        Instant createdAt) {
}
