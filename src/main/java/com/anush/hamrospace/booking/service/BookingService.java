package com.anush.hamrospace.booking.service;

import java.util.UUID;

import com.anush.hamrospace.booking.dto.request.BookingCancelRequest;
import com.anush.hamrospace.booking.dto.request.BookingCreateRequest;
import com.anush.hamrospace.booking.dto.request.BookingFilterRequest;
import com.anush.hamrospace.booking.dto.response.BookingDetailedResponse;
import com.anush.hamrospace.booking.dto.response.BookingSummaryResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;

public interface BookingService {

    BookingDetailedResponse createBooking(BookingCreateRequest request);

    BookingDetailedResponse cancelBooking(UUID bookingId, BookingCancelRequest request);

    BookingDetailedResponse confirmBooking(UUID bookingId);

    BookingDetailedResponse confirmCheckIn(UUID bookingId);

    BookingDetailedResponse markNoShow(UUID bookingId);

    PageResponse<BookingSummaryResponse> getMyBookings(BookingFilterRequest filterRequest,PaginationRequest paginationRequest);

    BookingDetailedResponse getBookingById(UUID bookingId);

    void markBookingFeePaid(UUID bookingId);
}
