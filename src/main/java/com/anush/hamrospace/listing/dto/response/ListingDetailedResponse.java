package com.anush.hamrospace.listing.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.anush.hamrospace.amenity.dto.response.AmenityResponse;
import com.anush.hamrospace.listing.enums.ListingStatus;
import com.anush.hamrospace.listing.enums.PricingType;
import com.anush.hamrospace.listing.enums.PropertyType;
import com.anush.hamrospace.listing.enums.RoomType;

public record ListingDetailedResponse(
        UUID id,
        String title,
        String slug,
        String description,
        String tagline,
        BigDecimal rent,
        BigDecimal deposit,
        String address,
        Double latitude,
        Double longitude,
        ListingStatus status,
        PricingType pricingType,
        boolean verified,
        Integer maxGuests,
        Integer bedrooms,
        Integer beds,
        Double bathrooms,
        Integer areaSqFt,
        PropertyType propertyType,
        RoomType roomType,
        Instant availableFrom,
        Integer minStayMonths,
        boolean available,
        BigDecimal averageRating,
        Integer reviewCount,
        List<String> houseRules,
        List<ListingImageResponse> images,
        Set<AmenityResponse> amenities,
        HostResponse host,
        Instant createdAt,
        Instant updatedAt,
        String rejectionReason) {

}
