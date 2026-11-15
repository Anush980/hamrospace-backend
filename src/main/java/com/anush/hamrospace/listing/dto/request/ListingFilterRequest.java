package com.anush.hamrospace.listing.dto.request;
public record ListingFilterRequest(
    String search,
    Double latitude,
    Double longitude,
    String address
) {
    
}
