package com.anush.hamrospace.booking.service;

import java.util.List;
import java.util.UUID;

import com.anush.hamrospace.booking.dto.request.BookingCancelRequest;
import com.anush.hamrospace.booking.dto.request.BookingCreateRequest;
import com.anush.hamrospace.booking.dto.response.BookingDetailedResponse;
import com.anush.hamrospace.booking.dto.response.BookingSummaryResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;

public interface BookingService {

    BookingDetailedResponse createBooking(BookingCreateRequest request);

    BookingDetailedResponse cancelBooking(UUID bookingId, BookingCancelRequest request);

    BookingDetailedResponse confirmCheckIn(UUID bookingId);

    BookingDetailedResponse markNoShow(UUID bookingId);

    PageResponse<BookingSummaryResponse> getMyBookings();

    BookingDetailedResponse getBookingById(UUID bookingId, UUID userId);

    void markBookingFeePaid(UUID bookingId);
}
