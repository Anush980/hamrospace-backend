package com.anush.hamrospace.booking.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingCreateRequest(
    
    @NotNull(message = "Listing ID is required")
    UUID listingId,

    @NotNull(message = "Move-in date is required")
    @Future(message = "Move-in date must be in the future")
    Instant moveInDate,

    @NotNull(message = "Monthly rent is required")
    @Min(value = 0, message = "Monthly rent must be positive")
    BigDecimal monthlyRent,

    @Min(value = 1, message = "At least 1 guest")
    Integer numberOfGuests,

    String notes
) {
    
}
