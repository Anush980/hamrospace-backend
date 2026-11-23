package com.anush.hamrospace.booking.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.anush.hamrospace.booking.enums.BookingFeeStatus;
import com.anush.hamrospace.booking.enums.BookingStatus;

public record BookingSummaryResponse(
    UUID id,

    UUID listingId,
    String listingTitle,
    String listingSlug,

    BigDecimal monthlyRent,
    BigDecimal bookingFeeAmount,

    Instant moveInDate,
    BookingStatus status,
    BookingFeeStatus bookingFeeStatus,
    
    Instant checkedInAt,
    Instant createdAt
) {
    
}
