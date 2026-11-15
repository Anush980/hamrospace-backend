package com.anush.hamrospace.listing.dto.response;

import java.util.UUID;

public record HostResponse(
                UUID userId,
                String email,
                String fullName,
                String profileImageUrl
        ) {}
