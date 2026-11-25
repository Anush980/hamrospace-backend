package com.anush.hamrospace.review.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID listingId,
        UUID authorId,
        String authorName,
        Integer rating,
        String comment,
        boolean hidden,
        Instant createdAt
) {
}
