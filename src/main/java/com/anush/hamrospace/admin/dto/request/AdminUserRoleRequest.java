package com.anush.hamrospace.admin.dto.request;

import com.anush.hamrospace.user.enums.UserRole;

import jakarta.validation.constraints.NotNull;

public record AdminUserRoleRequest(
        @NotNull(message = "Role is required") UserRole role) {
}
