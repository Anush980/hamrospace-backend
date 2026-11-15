package com.anush.hamrospace.listing.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.geo.Point;

import com.anush.hamrospace.listing.enums.ListingStatus;
import com.anush.hamrospace.listing.enums.PricingType;
import com.anush.hamrospace.listing.enums.PropertyType;
import com.anush.hamrospace.listing.enums.RoomType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ListingUpdateRequest(

    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,

    String description,

    @Size(max = 150, message = "Tagline must not exceed 150 characters")
    String tagline,

    @DecimalMin(value = "0.0", inclusive = false, message = "Rent must be greater than zero")
    BigDecimal rent,

    BigDecimal deposit,

    String address,

    Double latitude,

    Double longitude,

    ListingStatus status,

    PricingType pricingType,

    Boolean verified,

    Boolean available,

    @Min(value = 1, message = "Max guests must be at least 1")
    Integer maxGuests,

    @Min(value = 1, message = "Bedrooms must be at least 1")
    Integer bedrooms,

    @Min(value = 1, message = "Beds must be at least 1")
    Integer beds,

    @DecimalMin(value = "0.5", message = "Bathrooms must be at least 0.5")
    Double bathrooms,

    Integer areaSqFt,

    PropertyType propertyType,

    RoomType roomType,

    Instant availableFrom,

    @Min(value = 1, message = "Minimum stay months must be at least 1")
    Integer minStayMonths,

    List<String> houseRules,

    Set<UUID> amenityIds
) {}