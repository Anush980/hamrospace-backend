package com.anush.hamrospace.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.anush.hamrospace.booking.dto.request.BookingCreateRequest;
import com.anush.hamrospace.booking.dto.response.BookingDetailedResponse;
import com.anush.hamrospace.booking.dto.response.BookingSummaryResponse;
import com.anush.hamrospace.booking.entity.Booking;
import com.anush.hamrospace.common.mapper.MapStructMapperHelper;

@Mapper(componentModel = "spring",uses={MapStructMapperHelper.class},nullValuePropertyMappingStrategy=NullValuePropertyMappingStrategy.IGNORE)
public interface BookingMapper {
    
Booking toEntity(BookingCreateRequest request);

BookingSummaryResponse toSummaryResponse(Booking booking);

BookingDetailedResponse toDetailedResponse(Booking booking);

}
