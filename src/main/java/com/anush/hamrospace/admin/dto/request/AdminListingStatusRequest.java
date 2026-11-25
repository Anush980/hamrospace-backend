package com.anush.hamrospace.admin.dto.request;

import com.anush.hamrospace.listing.enums.ListingStatus;

import jakarta.validation.constraints.NotNull;

public record AdminListingStatusRequest(
        @NotNull(message = "Status is required") ListingStatus status,
        String reason
) {
}
