package com.anush.hamrospace.booking.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.anush.hamrospace.booking.enums.BookingFeeStatus;
import com.anush.hamrospace.booking.enums.BookingStatus;

public record BookingDetailedResponse(
    UUID id,
     UUID listingId,
    String listingTitle,
    String listingAddress,

    BigDecimal monthlyRent,
    BigDecimal bookingFeeAmount,

    UUID guestId,
    String guestName,
    String guestEmail,
    Integer numberOfGuests,
    Instant moveInDate,

    BookingStatus status,
    BookingFeeStatus bookingFeeStatus,

    BigDecimal refundAmount,
    String cancellationReason,
    Instant cancelledAt,

    Instant checkedInAt,
    String notes,
    
    Instant createdAt,
    Instant updatedAt
) {
    
}
