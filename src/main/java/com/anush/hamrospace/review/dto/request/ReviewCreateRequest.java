package com.anush.hamrospace.review.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @NotNull(message = "Booking id is required") UUID bookingId,
        @NotNull(message = "Listing id is required") UUID listingId,
        @NotNull(message = "Rating is required") @Min(1) @Max(5) Integer rating,
        // @NotBlank(message = "Comment is required") 
        @Size(max = 2000) String comment
) {
}
