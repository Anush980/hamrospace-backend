package com.anush.hamrospace.booking.dto.request;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookingCreateRequest(

    @NotNull(message = "Listing ID is required")
    UUID listingId,

    @NotNull(message = "Move-in date is required")
    @Future(message = "Move-in date must be in the future")
    Instant moveInDate,

    @Min(value = 1, message = "At least 1 guest")
    Integer numberOfGuests,

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    String notes
) {

}
