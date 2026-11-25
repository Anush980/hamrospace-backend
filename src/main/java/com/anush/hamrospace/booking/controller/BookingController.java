package com.anush.hamrospace.booking.controller;

import com.anush.hamrospace.booking.dto.request.BookingCancelRequest;
import com.anush.hamrospace.booking.dto.request.BookingCreateRequest;
import com.anush.hamrospace.booking.dto.request.BookingFilterRequest;
import com.anush.hamrospace.booking.dto.response.BookingDetailedResponse;
import com.anush.hamrospace.booking.dto.response.BookingSummaryResponse;
import com.anush.hamrospace.booking.service.BookingService;
import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION + ApiPath.BOOKING)
public class BookingController {

    private final BookingService bookingService;

    @GetMapping(ApiPath.ME)
    @Operation(summary = "Get all bookings")
    public ApiResponse<PageResponse<BookingSummaryResponse>> getMyBookings(
            @Valid BookingFilterRequest filterRequest,
            @Valid PaginationRequest paginationRequest) {
        return ApiResponse.ok("Bookings fetched successfully",
                bookingService.getMyBookings(filterRequest, paginationRequest));
    }

    @PostMapping("/create")
    @Operation(summary = "Create new booking")
    public ApiResponse<BookingDetailedResponse> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        return ApiResponse.ok("Booking created successfully", bookingService.createBooking(request));
    }

    @PatchMapping(ApiPath.ID)
    @Operation(summary = "Cancel existing booking")
    public ApiResponse<BookingDetailedResponse> cancelBooking(
            @PathVariable("id") UUID bookingId,
            @Valid @RequestBody BookingCancelRequest cancelRequest) {
        return ApiResponse.ok("Booking cancelled successfully",
                bookingService.cancelBooking(bookingId, cancelRequest));
    }

    @GetMapping(ApiPath.ID)
    @Operation(summary = "Get booking by id")
    public ApiResponse<BookingDetailedResponse> getBookingById(@PathVariable("id") UUID bookingId) {
        return ApiResponse.ok("Booking fetched successfully", bookingService.getBookingById(bookingId));
    }

    @PatchMapping(ApiPath.ID + "/confirm")
    @Operation(summary = "Confirm a booking (host only)")
    public ApiResponse<BookingDetailedResponse> confirmBooking(@PathVariable("id") UUID bookingId) {
        return ApiResponse.ok("Booking confirmed successfully", bookingService.confirmBooking(bookingId));
    }

    @PatchMapping(ApiPath.ID + "/check-in")
    @Operation(summary = "Check in a guest (host only)")
    public ApiResponse<BookingDetailedResponse> confirmCheckIn(@PathVariable("id") UUID bookingId) {
        return ApiResponse.ok("Check-in confirmed successfully", bookingService.confirmCheckIn(bookingId));
    }

    @PatchMapping(ApiPath.ID + "/no-show")
    @Operation(summary = "Mark a guest as no-show (host only)")
    public ApiResponse<BookingDetailedResponse> markNoShow(@PathVariable("id") UUID bookingId) {
        return ApiResponse.ok("Booking marked as no-show successfully", bookingService.markNoShow(bookingId));
    }

    @PatchMapping(ApiPath.ID + "/fee-paid")
    @Operation(summary = "Mark the booking fee as paid (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> markBookingFeePaid(@PathVariable("id") UUID bookingId) {
        bookingService.markBookingFeePaid(bookingId);
        return ApiResponse.ok("Booking fee marked as paid successfully");
    }
}
