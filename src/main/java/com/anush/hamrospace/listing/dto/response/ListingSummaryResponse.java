package com.anush.hamrospace.listing.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.anush.hamrospace.listing.enums.PricingType;
import com.anush.hamrospace.listing.enums.PropertyType;
import com.anush.hamrospace.listing.enums.RoomType;

public record ListingSummaryResponse(
    UUID id,
    String title,
    String slug,
    String tagline,
    BigDecimal rent,
    PricingType pricingType,
    String address,
    PropertyType propertyType,
    RoomType roomType,
    boolean verified,
    boolean available,
    BigDecimal averageRating,
    Integer reviewCount,
    String coverImageUrl,
    String rejectionReason
) {
    
}
