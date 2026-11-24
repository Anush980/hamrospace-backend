package com.anush.hamrospace.booking.repository;

import com.anush.hamrospace.booking.entity.Booking;
import com.anush.hamrospace.common.repository.AbstractRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.user.entity.User;



public interface BookingRepository extends AbstractRepository<Booking> {

    Optional<Booking> findByIdAndUserIdAndDeletedAtIsNull(UUID bookingId,UUID userId);
    
    List<Booking> findByListingAndDeletedAtIsNull(Listing listing);

    List<Booking> findByGuestAndDeletedAtIsNull(User guest);

}
