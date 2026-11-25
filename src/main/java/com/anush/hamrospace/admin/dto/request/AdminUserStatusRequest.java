package com.anush.hamrospace.admin.dto.request;

import com.anush.hamrospace.user.enums.UserStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUserStatusRequest(
        @NotNull(message = "Status is required") UserStatus status,

        @Size(max = 500, message = "Reason must not exceed 500 characters") String reason) {
}
