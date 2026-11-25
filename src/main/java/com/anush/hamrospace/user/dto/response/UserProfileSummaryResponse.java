package com.anush.hamrospace.user.dto.response;

import java.util.UUID;

import com.anush.hamrospace.user.enums.PreferredLanguage;
import com.anush.hamrospace.user.enums.UiTheme;
import com.anush.hamrospace.user.enums.UserRole;

public record UserProfileSummaryResponse(
        UUID userId,
        String email,
        String fullName,
        UserRole role,
        String profileImageUrl,
        PreferredLanguage preferredLanguage,
        UiTheme uiTheme
) {}