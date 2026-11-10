package com.anush.hamrospace.listing.dto.request;

public record ListingFilterRequest(
    String search,
    String latitude,
    String longitude,
    String address
) {
    
}
