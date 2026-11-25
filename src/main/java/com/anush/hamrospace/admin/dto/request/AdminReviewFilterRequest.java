package com.anush.hamrospace.admin.dto.request;

import java.util.UUID;

public record AdminReviewFilterRequest(
        UUID listingId,
        Boolean hidden,
        String keyword
) {
}
