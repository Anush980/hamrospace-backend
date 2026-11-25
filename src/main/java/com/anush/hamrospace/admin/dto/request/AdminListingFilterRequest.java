package com.anush.hamrospace.admin.dto.request;

import com.anush.hamrospace.listing.enums.ListingStatus;

public record AdminListingFilterRequest(
        String keyword,
        ListingStatus status
) {
}
