package com.anush.hamrospace.listing.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.anush.hamrospace.listing.enums.PricingType;
import com.anush.hamrospace.listing.enums.PropertyType;
import com.anush.hamrospace.listing.enums.RoomType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ListingCreateRequest(

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,

    @NotBlank(message = "Description is required")
    String description,

    @Size(max = 150, message = "Tagline must not exceed 150 characters")
    String tagline,

    @NotNull(message = "Rent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rent must be greater than zero")
    BigDecimal rent,

    BigDecimal deposit,

    @NotBlank(message = "Address is required")
    String address,

    @NotNull(message = "Latitude is required")
    Double latitude,

    @NotNull(message = "Longitude is required")
    Double longitude,

    @NotNull(message = "Pricing type is required")
    PricingType pricingType,

    @NotNull(message = "Property type is required")
    PropertyType propertyType,

    @NotNull(message = "Room type is required")
    RoomType roomType,

    @Min(value = 1, message = "Max guests must be at least 1")
    Integer maxGuests,

    @Min(value = 1, message = "Bedrooms must be at least 1")
    Integer bedrooms,

    @Min(value = 1, message = "Beds must be at least 1")
    Integer beds,

    @DecimalMin(value = "1", message = "Bathrooms must be at least 1")
    Double bathrooms,

    Integer areaSqFt,

    Instant availableFrom,

    @Min(value = 1, message = "Minimum stay months must be at least 1")
    Integer minStayMonths,

    List<String> houseRules,

    Set<UUID> amenityIds
) {}