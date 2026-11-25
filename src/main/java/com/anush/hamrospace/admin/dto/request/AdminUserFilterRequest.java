package com.anush.hamrospace.admin.dto.request;

import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;

public record AdminUserFilterRequest(
        String keyword,
        UserRole role,
        UserStatus status) {
}
