package com.anush.hamrospace.booking.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.anush.hamrospace.booking.dto.request.BookingCancelRequest;
import com.anush.hamrospace.booking.dto.request.BookingCreateRequest;
import com.anush.hamrospace.booking.dto.response.BookingDetailedResponse;
import com.anush.hamrospace.booking.dto.response.BookingSummaryResponse;
import com.anush.hamrospace.booking.mapper.BookingMapper;
import com.anush.hamrospace.booking.repository.BookingRepository;
import com.anush.hamrospace.booking.service.BookingService;
import com.anush.hamrospace.common.dto.response.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    


    @Override
    public BookingDetailedResponse createBooking(BookingCreateRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createBooking'");
    }

    @Override
    public BookingDetailedResponse cancelBooking(UUID bookingId, BookingCancelRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelBooking'");
    }

    @Override
    public BookingDetailedResponse confirmCheckIn(UUID bookingId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'confirmCheckIn'");
    }

    @Override
    public BookingDetailedResponse markNoShow(UUID bookingId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'markNoShow'");
    }

    @Override
    public PageResponse<BookingSummaryResponse> getMyBookings() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMyBookings'");
    }

    @Override
    public BookingDetailedResponse getBookingById(UUID bookingId, UUID userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBookingById'");
    }

    @Override
    public void markBookingFeePaid(UUID bookingId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'markBookingFeePaid'");
    }
    
}
