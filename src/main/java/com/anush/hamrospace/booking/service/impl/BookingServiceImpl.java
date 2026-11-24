package com.anush.hamrospace.booking.service.impl;

import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.repository.UserRepository;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anush.hamrospace.booking.dto.request.BookingCancelRequest;
import com.anush.hamrospace.booking.dto.request.BookingCreateRequest;
import com.anush.hamrospace.booking.dto.request.BookingFilterRequest;
import com.anush.hamrospace.booking.dto.response.BookingDetailedResponse;
import com.anush.hamrospace.booking.dto.response.BookingSummaryResponse;
import com.anush.hamrospace.booking.entity.Booking;
import com.anush.hamrospace.booking.enums.BookingFeeStatus;
import com.anush.hamrospace.booking.enums.BookingStatus;
import com.anush.hamrospace.booking.mapper.BookingMapper;
import com.anush.hamrospace.booking.repository.BookingRepository;
import com.anush.hamrospace.booking.service.BookingService;
import com.anush.hamrospace.booking.specification.BookingSpecification;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.UnauthorizedAccessException;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.listing.entity.Listing;
import org.springframework.data.jpa.domain.Specification;
import com.anush.hamrospace.listing.repository.ListingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final ListingRepository listingRepository;
    


    @Override
    public BookingDetailedResponse createBooking(BookingCreateRequest request) {
        
        UUID guestId = SecurityUtil.getCurrentUserId();
        User guest = userRepository.getReferenceById(guestId);

        Listing listing = listingRepository.findByIdAndDeletedAtIsNull(request.listingId()).orElseThrow(()-> new BadRequestException("Listing not found"));

        Booking booking = bookingMapper.toEntity(request);
        booking.setGuest(guest);
        booking.setListing(listing);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDetailedResponse(savedBooking);
    }

    @Override
    public BookingDetailedResponse cancelBooking(UUID bookingId, BookingCancelRequest request) {
       UUID guestId = SecurityUtil.getCurrentUserId();

        Booking  booking= bookingRepository.findByIdAndUserIdAndDeletedAtIsNull(bookingId,guestId).orElseThrow(()-> new BadRequestException("booking not found"));

        if(!(booking.getStatus()==BookingStatus.CONFIRMED || booking.getStatus()==BookingStatus.PENDING )){
            throw new UnauthorizedAccessException("You can't cancel this booking anymore");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingMapper.toDetailedResponse(booking);
    }

    @Override
    public BookingDetailedResponse confirmCheckIn(UUID bookingId) {
         UUID guestId = SecurityUtil.getCurrentUserId();

        Booking  booking= bookingRepository.findByIdAndUserIdAndDeletedAtIsNull(bookingId,guestId).orElseThrow(()-> new BadRequestException("booking not found"));

        if(booking.getStatus()==BookingStatus.CONFIRMED || booking.getStatus()==BookingStatus.CANCELLED ){
            throw new UnauthorizedAccessException("You can't confirm this booking anymore");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        return bookingMapper.toDetailedResponse(booking);
    }

    @Override
    public BookingDetailedResponse markNoShow(UUID bookingId) {
        UUID guestId = SecurityUtil.getCurrentUserId();

        Booking  booking= bookingRepository.findByIdAndUserIdAndDeletedAtIsNull(bookingId,guestId).orElseThrow(()-> new BadRequestException("booking not found"));

        if((booking.getStatus()==BookingStatus.NO_SHOW || booking.getStatus()==BookingStatus.CANCELLED || booking.getStatus()==BookingStatus.CHECKED_IN)){
            throw new UnauthorizedAccessException("You can't perform this action");
        }

        booking.setStatus(BookingStatus.NO_SHOW);
        return bookingMapper.toDetailedResponse(booking);  }

    @Override
    public PageResponse<BookingSummaryResponse> getMyBookings(BookingFilterRequest filterRequest,PaginationRequest paginationRequest) {
        // UUID guestId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PaginationUtil.of(paginationRequest);

        Specification<Booking> spec = BookingSpecification.buildSpecification(filterRequest);
        Page<Booking> bookingPage = bookingRepository.findAll(spec,pageable);
        Page<BookingSummaryResponse> responsePage = bookingPage.map(bookingMapper::toSummaryResponse);
        return PageResponse.from(responsePage);

          }

    @Override
    public BookingDetailedResponse getBookingById(UUID bookingId) {
       UUID guestId = SecurityUtil.getCurrentUserId();

        Booking  booking= bookingRepository.findByIdAndUserIdAndDeletedAtIsNull(bookingId,guestId).orElseThrow(()-> new BadRequestException("booking not found"));

        return bookingMapper.toDetailedResponse(booking);
    }

    @Override
    public void markBookingFeePaid(UUID bookingId) {
       UUID guestId = SecurityUtil.getCurrentUserId();

        Booking  booking= bookingRepository.findByIdAndUserIdAndDeletedAtIsNull(bookingId,guestId).orElseThrow(()-> new BadRequestException("booking not found"));

        if((booking.getBookingFeeStatus()==BookingFeeStatus.PAID|| booking.getBookingFeeStatus()==BookingFeeStatus.REFUNDED )){
            throw new UnauthorizedAccessException("You can't perform this action");
        }
        booking.setBookingFeeStatus(BookingFeeStatus.PAID);

    }
}
