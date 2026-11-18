package com.anush.hamrospace.listing.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.anush.hamrospace.listing.enums.PropertyType;

public record ListingFilterRequest(
    String keyword,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Double latitude,
    Double longitude,
    Double radiusKm,
    PropertyType propertyType,
    String address,
    Boolean verifiedOnly,
    List<UUID> amenityIds
) {
    
}
