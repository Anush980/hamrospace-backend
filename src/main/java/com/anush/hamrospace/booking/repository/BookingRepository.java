package com.anush.hamrospace.booking.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import com.anush.hamrospace.booking.entity.Booking;
import com.anush.hamrospace.booking.enums.BookingStatus;
import com.anush.hamrospace.common.repository.AbstractRepository;

public interface BookingRepository extends AbstractRepository<Booking> {

    Optional<Booking> findByIdAndGuestIdAndDeletedAtIsNull(UUID bookingId, UUID userId);

    Optional<Booking> findByIdAndListingHostIdAndDeletedAtIsNull(UUID bookingId, UUID hostId);

    boolean existsByListingIdAndMoveInDateAndStatusIn(UUID listingId, Instant moveInDate,
            Collection<BookingStatus> statuses);
}
