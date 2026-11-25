package com.anush.hamrospace.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.anush.hamrospace.booking.dto.request.BookingCreateRequest;
import com.anush.hamrospace.booking.dto.response.BookingDetailedResponse;
import com.anush.hamrospace.booking.dto.response.BookingSummaryResponse;
import com.anush.hamrospace.booking.entity.Booking;
import com.anush.hamrospace.common.mapper.MapStructMapperHelper;

@Mapper(componentModel = "spring", uses = { MapStructMapperHelper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookingMapper {

    @Mapping(target = "monthlyRent", ignore = true)
    @Mapping(target = "bookingFeeAmount", ignore = true)
    @Mapping(target = "numberOfGuests", ignore = true)
    Booking toEntity(BookingCreateRequest request);

    @Mapping(target = "listingId", source = "listing.id")
    @Mapping(target = "listingTitle", source = "listing.title")
    @Mapping(target = "listingSlug", source = "listing.slug")
    BookingSummaryResponse toSummaryResponse(Booking booking);

    @Mapping(target = "listingId", source = "listing.id")
    @Mapping(target = "listingTitle", source = "listing.title")
    @Mapping(target = "listingAddress", source = "listing.address")
    @Mapping(target = "guestId", source = "guest.id")
    @Mapping(target = "guestName", source = "guest.profile.fullName")
    @Mapping(target = "guestEmail", source = "guest.email")
    BookingDetailedResponse toDetailedResponse(Booking booking);
}
