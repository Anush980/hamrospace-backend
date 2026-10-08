package com.anush.hamrospace.user.dto.response;

import java.util.UUID;

import com.anush.hamrospace.user.enums.PreferredLanguage;
import com.anush.hamrospace.user.enums.UiTheme;

public record UserProfileSummaryResponse(
        UUID userId,
        String email,
        String fullName,
        String profileImageUrl,
        PreferredLanguage preferredLanguage,
        UiTheme uiTheme
) {}