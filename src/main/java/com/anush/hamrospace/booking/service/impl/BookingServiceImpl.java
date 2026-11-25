package com.anush.hamrospace.booking.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.anush.hamrospace.core.config.booking.BookingProperties;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.ConflictException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.listing.enums.ListingStatus;
import com.anush.hamrospace.listing.repository.ListingRepository;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final ListingRepository listingRepository;
    private final BookingProperties bookingProperties;

    @Override
    public BookingDetailedResponse createBooking(BookingCreateRequest request) {
        UUID guestId = SecurityUtil.getCurrentUserId();
        User guest = userRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findByIdAndDeletedAtIsNull(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.isActive() || !listing.isAvailable() || listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ConflictException("Listing is not available for booking");
        }
        if (listing.getAvailableFrom() != null && request.moveInDate().isBefore(listing.getAvailableFrom())) {
            throw new ConflictException("Move-in date is before the listing's available date");
        }

        int maxGuests = listing.getMaxGuests() != null ? listing.getMaxGuests() : 1;
        int numberOfGuests = request.numberOfGuests() != null ? request.numberOfGuests() : 1;
        if (numberOfGuests > maxGuests) {
            throw new ConflictException("Number of guests exceeds the maximum allowed for this listing");
        }

        if (bookingRepository.existsByListingIdAndMoveInDateAndStatusIn(
                request.listingId(),
                request.moveInDate(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN))) {
            throw new ConflictException("Listing is already booked for the selected date");
        }

        BigDecimal feePercentage = bookingProperties.getFeePercentage() != null
                ? bookingProperties.getFeePercentage()
                : BigDecimal.TEN;

        Booking booking = bookingMapper.toEntity(request);
        booking.setGuest(guest);
        booking.setListing(listing);
        booking.setNumberOfGuests(numberOfGuests);
        booking.setMonthlyRent(listing.getRent());
        booking.setBookingFeeAmount(listing.getRent()
                .multiply(feePercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDetailedResponse(savedBooking);
    }

    @Override
    public BookingDetailedResponse cancelBooking(UUID bookingId, BookingCancelRequest request) {
        UUID guestId = SecurityUtil.getCurrentUserId();
        Booking booking = bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(bookingId, guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException("Booking cannot be cancelled in its current status");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.setCancellationReason(request.cancellationReason());

        if (booking.getBookingFeeStatus() == BookingFeeStatus.PAID) {
            booking.setRefundAmount(booking.getBookingFeeAmount());
            booking.setBookingFeeStatus(BookingFeeStatus.REFUNDED);
        } else {
            booking.setRefundAmount(BigDecimal.ZERO);
        }

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDetailedResponse(savedBooking);
    }

    @Override
    public BookingDetailedResponse confirmBooking(UUID bookingId) {
        Booking booking = findBookingForHost(bookingId, SecurityUtil.getCurrentUserId());

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Only pending bookings can be confirmed");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDetailedResponse(savedBooking);
    }

    @Override
    public BookingDetailedResponse confirmCheckIn(UUID bookingId) {
        Booking booking = findBookingForHost(bookingId, SecurityUtil.getCurrentUserId());

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException("Only confirmed bookings can be checked in");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(Instant.now());
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDetailedResponse(savedBooking);
    }

    @Override
    public BookingDetailedResponse markNoShow(UUID bookingId) {
        Booking booking = findBookingForHost(bookingId, SecurityUtil.getCurrentUserId());

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException("Booking cannot be marked as no-show in its current status");
        }

        booking.setStatus(BookingStatus.NO_SHOW);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDetailedResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingSummaryResponse> getMyBookings(BookingFilterRequest filterRequest,
            PaginationRequest paginationRequest) {
        UUID guestId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PaginationUtil.of(paginationRequest);

        Specification<Booking> spec = BookingSpecification.buildSpecification(guestId, filterRequest);
        Page<Booking> bookingPage = bookingRepository.findAll(spec, pageable);
        Page<BookingSummaryResponse> responsePage = bookingPage.map(bookingMapper::toSummaryResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailedResponse getBookingById(UUID bookingId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        Booking booking = bookingRepository.findById(bookingId)
                .or(() -> bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(bookingId, currentUserId))
                .or(() -> bookingRepository.findByIdAndListingHostIdAndDeletedAtIsNull(bookingId, currentUserId))
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!isGuest(booking, currentUserId) && !isHost(booking, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this booking");
        }

        return bookingMapper.toDetailedResponse(booking);
    }

    @Override
    public void markBookingFeePaid(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getBookingFeeStatus() != BookingFeeStatus.PENDING) {
            throw new ConflictException("Booking fee has already been processed");
        }

        booking.setBookingFeeStatus(BookingFeeStatus.PAID);
        bookingRepository.save(booking);
    }

    private Booking findBookingForHost(UUID bookingId, UUID hostId) {
        Booking booking = bookingRepository.findByIdAndListingHostIdAndDeletedAtIsNull(bookingId, hostId)
                .or(() -> bookingRepository.findById(bookingId))
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!isHost(booking, hostId)) {
            throw new AccessDeniedException("Only the listing host can perform this action");
        }
        return booking;
    }

    private boolean isGuest(Booking booking, UUID userId) {
        return booking.getGuest() != null && userId.equals(booking.getGuest().getId());
    }

    private boolean isHost(Booking booking, UUID userId) {
        return booking.getListing() != null
                && booking.getListing().getHost() != null
                && userId.equals(booking.getListing().getHost().getId());
    }
}
