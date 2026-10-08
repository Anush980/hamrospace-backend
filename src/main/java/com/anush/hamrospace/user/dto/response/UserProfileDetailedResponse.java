package com.anush.hamrospace.user.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.anush.hamrospace.user.enums.AddressType;
import com.anush.hamrospace.user.enums.PreferredLanguage;
import com.anush.hamrospace.user.enums.SubscriptionType;
import com.anush.hamrospace.user.enums.UiTheme;

public record UserProfileDetailedResponse(
        UUID userId,
        String email,
        String fullName,
        String phoneNumber,
        AddressType addressType,
        String address,
        String profileImageUrl,
        String bio,
        PreferredLanguage preferredLanguage,
        SubscriptionType subscriptionType,
        UiTheme uiTheme,
        boolean isEmailVerified,
        Instant createdAt,
        Instant updatedAt
) {}