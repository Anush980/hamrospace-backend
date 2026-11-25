package com.anush.hamrospace.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
import com.anush.hamrospace.booking.service.impl.BookingServiceImpl;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.config.booking.BookingProperties;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.ConflictException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.user.UserPrincipal;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.listing.enums.ListingStatus;
import com.anush.hamrospace.listing.repository.ListingRepository;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;
import com.anush.hamrospace.user.repository.UserRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private static final UUID GUEST_ID = UUID.randomUUID();
    private static final UUID HOST_ID = UUID.randomUUID();
    private static final UUID OTHER_USER_ID = UUID.randomUUID();
    private static final UUID LISTING_ID = UUID.randomUUID();
    private static final UUID BOOKING_ID = UUID.randomUUID();
    private static final BigDecimal RENT = new BigDecimal("10.10");

    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private ListingRepository listingRepository;
    @Mock
    private BookingProperties bookingProperties;

    @InjectMocks
    private BookingServiceImpl service;

    @BeforeEach
    void setUp() {
        authenticate(GUEST_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── createBooking ────────────────────────────────────────────────────────

    @Test
    void createBookingDerivesRentAndRoundedFeeAndDefaultsGuestCount() {
        BookingCreateRequest request = bookingRequest(null);
        Listing listing = bookableListing();
        BookingDetailedResponse expected = mock(BookingDetailedResponse.class);

        when(userRepository.findById(GUEST_ID)).thenReturn(Optional.of(user(GUEST_ID)));
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        when(bookingRepository.existsByListingIdAndMoveInDateAndStatusIn(
                eq(LISTING_ID), eq(request.moveInDate()), anyCollection())).thenReturn(false);
        when(bookingProperties.getFeePercentage()).thenReturn(new BigDecimal("5"));
        when(bookingMapper.toEntity(request)).thenReturn(new Booking());
        saveReturnsArgument();
        when(bookingMapper.toDetailedResponse(any(Booking.class))).thenReturn(expected);

        BookingDetailedResponse result = service.createBooking(request);

        assertThat(result).isSameAs(expected);
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking saved = captor.getValue();
        assertThat(saved.getGuest().getId()).isEqualTo(GUEST_ID);
        assertThat(saved.getListing()).isSameAs(listing);
        assertThat(saved.getMonthlyRent()).isEqualByComparingTo("10.10");
        assertThat(saved.getBookingFeeAmount()).isEqualByComparingTo("0.51");
        assertThat(saved.getBookingFeeAmount().scale()).isEqualTo(2);
        assertThat(saved.getNumberOfGuests()).isEqualTo(1);
    }

    @Test
    void createBookingKeepsRequestedGuestCountWithinListingCapacity() {
        BookingCreateRequest request = bookingRequest(3);
        BookingDetailedResponse expected = mock(BookingDetailedResponse.class);

        when(userRepository.findById(GUEST_ID)).thenReturn(Optional.of(user(GUEST_ID)));
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(bookableListing()));
        when(bookingRepository.existsByListingIdAndMoveInDateAndStatusIn(
                eq(LISTING_ID), eq(request.moveInDate()), anyCollection())).thenReturn(false);
        when(bookingProperties.getFeePercentage()).thenReturn(new BigDecimal("10"));
        when(bookingMapper.toEntity(request)).thenReturn(new Booking());
        saveReturnsArgument();
        when(bookingMapper.toDetailedResponse(any(Booking.class))).thenReturn(expected);

        assertThat(service.createBooking(request)).isSameAs(expected);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().getNumberOfGuests()).isEqualTo(3);
        assertThat(captor.getValue().getBookingFeeAmount()).isEqualByComparingTo("1.01");
    }

    @Test
    void createBookingFailsWhenListingDoesNotExist() {
        when(userRepository.findById(GUEST_ID)).thenReturn(Optional.of(user(GUEST_ID)));
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBooking(bookingRequest(1)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsInactiveListing() {
        assertCreateConflict(listing -> listing.setActive(false));
    }

    @Test
    void createBookingRejectsUnavailableListing() {
        assertCreateConflict(listing -> listing.setAvailable(false));
    }

    @Test
    void createBookingRejectsPendingListing() {
        assertCreateConflict(listing -> listing.setStatus(ListingStatus.PENDING));
    }

    @Test
    void createBookingRejectsMoveInBeforeAvailableFrom() {
        Listing listing = bookableListing();
        listing.setAvailableFrom(Instant.now().plus(10, ChronoUnit.DAYS));
        BookingCreateRequest request = new BookingCreateRequest(
                LISTING_ID, Instant.now().plus(1, ChronoUnit.DAYS), 1, null);

        when(userRepository.findById(GUEST_ID)).thenReturn(Optional.of(user(GUEST_ID)));
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> service.createBooking(request)).isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsTooManyGuests() {
        Listing listing = bookableListing();
        listing.setMaxGuests(2);

        when(userRepository.findById(GUEST_ID)).thenReturn(Optional.of(user(GUEST_ID)));
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> service.createBooking(bookingRequest(3)))
                .isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingRejectsOverlappingActiveBooking() {
        BookingCreateRequest request = bookingRequest(1);

        when(userRepository.findById(GUEST_ID)).thenReturn(Optional.of(user(GUEST_ID)));
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(bookableListing()));
        when(bookingRepository.existsByListingIdAndMoveInDateAndStatusIn(
                eq(LISTING_ID), eq(request.moveInDate()), anyCollection())).thenReturn(true);

        assertThatThrownBy(() -> service.createBooking(request)).isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    // ── cancelBooking ────────────────────────────────────────────────────────

    @Test
    void cancelBookingRefundsPaidFeeAndPersistsCancellationDetails() {
        Booking booking = booking(BookingStatus.CONFIRMED, BookingFeeStatus.PAID);
        BookingDetailedResponse expected = mock(BookingDetailedResponse.class);
        BookingCancelRequest request = new BookingCancelRequest("Plans changed");

        when(bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID))
                .thenReturn(Optional.of(booking));
        saveReturnsArgument();
        when(bookingMapper.toDetailedResponse(any(Booking.class))).thenReturn(expected);

        assertThat(service.cancelBooking(BOOKING_ID, request)).isSameAs(expected);

        verify(bookingRepository).save(booking);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancellationReason()).isEqualTo("Plans changed");
        assertThat(booking.getCancelledAt()).isNotNull();
        assertThat(booking.getRefundAmount()).isEqualByComparingTo("50.00");
        assertThat(booking.getBookingFeeStatus()).isEqualTo(BookingFeeStatus.REFUNDED);
    }

    @Test
    void cancelBookingWithUnpaidFeeUsesZeroRefund() {
        Booking booking = booking(BookingStatus.PENDING, BookingFeeStatus.PENDING);
        when(bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID))
                .thenReturn(Optional.of(booking));
        saveReturnsArgument();
        when(bookingMapper.toDetailedResponse(any(Booking.class)))
                .thenReturn(mock(BookingDetailedResponse.class));

        service.cancelBooking(BOOKING_ID, new BookingCancelRequest("Not needed anymore"));

        verify(bookingRepository).save(booking);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getRefundAmount()).isEqualByComparingTo("0");
        assertThat(booking.getBookingFeeStatus()).isEqualTo(BookingFeeStatus.PENDING);
    }

    @Test
    void cancelBookingRejectsInvalidStatus() {
        Booking booking = booking(BookingStatus.CHECKED_IN, BookingFeeStatus.PAID);
        when(bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.cancelBooking(BOOKING_ID, new BookingCancelRequest("Too late")))
                .isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    // ── host actions ─────────────────────────────────────────────────────────

    @Test
    void confirmBookingTransitionsPendingToConfirmed() {
        authenticate(HOST_ID);
        Booking booking = booking(BookingStatus.PENDING, BookingFeeStatus.PENDING);
        when(bookingRepository.findByIdAndListingHostIdAndDeletedAtIsNull(BOOKING_ID, HOST_ID))
                .thenReturn(Optional.of(booking));
        saveReturnsArgument();
        when(bookingMapper.toDetailedResponse(any(Booking.class)))
                .thenReturn(mock(BookingDetailedResponse.class));

        service.confirmBooking(BOOKING_ID);

        verify(bookingRepository).save(booking);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void confirmBookingDeniesNonHost() {
        authenticate(OTHER_USER_ID);
        Booking booking = booking(BookingStatus.PENDING, BookingFeeStatus.PENDING);
        when(bookingRepository.findByIdAndListingHostIdAndDeletedAtIsNull(BOOKING_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.confirmBooking(BOOKING_ID))
                .isInstanceOf(AccessDeniedException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void confirmBookingRejectsWrongStatus() {
        authenticate(HOST_ID);
        Booking booking = booking(BookingStatus.CONFIRMED, BookingFeeStatus.PENDING);
        when(bookingRepository.findByIdAndListingHostIdAndDeletedAtIsNull(BOOKING_ID, HOST_ID))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.confirmBooking(BOOKING_ID))
                .isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void confirmCheckInRequiresConfirmedStatusAndSetsTimestamp() {
        authenticate(HOST_ID);
        Booking booking = booking(BookingStatus.CONFIRMED, BookingFeeStatus.PENDING);
        when(bookingRepository.findByIdAndListingHostIdAndDeletedAtIsNull(BOOKING_ID, HOST_ID))
                .thenReturn(Optional.of(booking));
        saveReturnsArgument();
        when(bookingMapper.toDetailedResponse(any(Booking.class)))
                .thenReturn(mock(BookingDetailedResponse.class));

        service.confirmCheckIn(BOOKING_ID);

        verify(bookingRepository).save(booking);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CHECKED_IN);
        assertThat(booking.getCheckedInAt()).isNotNull();
    }

    @Test
    void confirmCheckInRejectsPendingBooking() {
        authenticate(HOST_ID);
        Booking booking = booking(BookingStatus.PENDING, BookingFeeStatus.PENDING);
        when(bookingRepository.findByIdAndListingHostIdAndDeletedAtIsNull(BOOKING_ID, HOST_ID))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.confirmCheckIn(BOOKING_ID))
                .isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = { "PENDING", "CONFIRMED" })
    void markNoShowAllowsPendingAndConfirmedBookings(BookingStatus status) {
        authenticate(HOST_ID);
        Booking booking = booking(status, BookingFeeStatus.PENDING);
        when(bookingRepository.findByIdAndListingHostIdAndDeletedAtIsNull(BOOKING_ID, HOST_ID))
                .thenReturn(Optional.of(booking));
        saveReturnsArgument();
        when(bookingMapper.toDetailedResponse(any(Booking.class)))
                .thenReturn(mock(BookingDetailedResponse.class));

        service.markNoShow(BOOKING_ID);

        verify(bookingRepository).save(booking);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.NO_SHOW);
    }

    @Test
    void markNoShowRejectsCheckedInBooking() {
        authenticate(HOST_ID);
        Booking booking = booking(BookingStatus.CHECKED_IN, BookingFeeStatus.PENDING);
        when(bookingRepository.findByIdAndListingHostIdAndDeletedAtIsNull(BOOKING_ID, HOST_ID))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.markNoShow(BOOKING_ID))
                .isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void markBookingFeePaidPersistsPaidStatus() {
        Booking booking = booking(BookingStatus.PENDING, BookingFeeStatus.PENDING);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        service.markBookingFeePaid(BOOKING_ID);

        verify(bookingRepository).save(booking);
        assertThat(booking.getBookingFeeStatus()).isEqualTo(BookingFeeStatus.PAID);
    }

    @Test
    void markBookingFeePaidRejectsAlreadyPaidBooking() {
        Booking booking = booking(BookingStatus.PENDING, BookingFeeStatus.PAID);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.markBookingFeePaid(BOOKING_ID))
                .isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    // ── reads ────────────────────────────────────────────────────────────────

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void getMyBookingsUsesCurrentGuestIdInSpecificationAndMapsPage() {
        Booking booking = booking(BookingStatus.PENDING, BookingFeeStatus.PENDING);
        BookingSummaryResponse summary = mock(BookingSummaryResponse.class);
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setPageNumber(2);
        paginationRequest.setPageSize(5);
        Pageable pageable = PaginationUtil.of(paginationRequest);

        when(bookingRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(booking), pageable, 1));
        when(bookingMapper.toSummaryResponse(booking)).thenReturn(summary);

        PageResponse<BookingSummaryResponse> result = service.getMyBookings(
                new BookingFilterRequest(null, null, null, null), paginationRequest);

        assertThat(result.getContent()).containsExactly(summary);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);

        ArgumentCaptor<Specification> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(bookingRepository).findAll(specCaptor.capture(), eq(pageable));

        Root root = mock(Root.class);
        Path guestPath = mock(Path.class);
        Path guestIdPath = mock(Path.class);
        when(root.get("deletedAt")).thenReturn(mock(Path.class));
        when(root.get("guest")).thenReturn(guestPath);
        when(guestPath.get("id")).thenReturn(guestIdPath);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        specCaptor.getValue().toPredicate(root, null, cb);
        verify(cb).equal(guestIdPath, GUEST_ID);
    }

    @Test
    void getBookingByIdAllowsGuest() {
        Booking booking = booking(BookingStatus.PENDING, BookingFeeStatus.PENDING);
        BookingDetailedResponse expected = mock(BookingDetailedResponse.class);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDetailedResponse(booking)).thenReturn(expected);

        assertThat(service.getBookingById(BOOKING_ID)).isSameAs(expected);
    }

    @Test
    void getBookingByIdAllowsHost() {
        authenticate(HOST_ID);
        Booking booking = booking(BookingStatus.CONFIRMED, BookingFeeStatus.PENDING);
        BookingDetailedResponse expected = mock(BookingDetailedResponse.class);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDetailedResponse(booking)).thenReturn(expected);

        assertThat(service.getBookingById(BOOKING_ID)).isSameAs(expected);
    }

    @Test
    void getBookingByIdDeniesUnrelatedUser() {
        authenticate(OTHER_USER_ID);
        Booking booking = booking(BookingStatus.PENDING, BookingFeeStatus.PENDING);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.getBookingById(BOOKING_ID))
                .isInstanceOf(AccessDeniedException.class);
        verify(bookingMapper, never()).toDetailedResponse(any());
    }

    @Test
    void getBookingByIdFailsWhenBookingIsMissing() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());
        when(bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID))
                .thenReturn(Optional.empty());
        when(bookingRepository.findByIdAndListingHostIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBookingById(BOOKING_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

    private void assertCreateConflict(Consumer<Listing> mutation) {
        Listing listing = bookableListing();
        mutation.accept(listing);
        when(userRepository.findById(GUEST_ID)).thenReturn(Optional.of(user(GUEST_ID)));
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> service.createBooking(bookingRequest(1)))
                .isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
    }

    private void saveReturnsArgument() {
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static BookingCreateRequest bookingRequest(Integer numberOfGuests) {
        return new BookingCreateRequest(
                LISTING_ID, Instant.now().plus(30, ChronoUnit.DAYS), numberOfGuests, "Please call first");
    }

    private static Booking booking(BookingStatus status, BookingFeeStatus feeStatus) {
        Booking booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setGuest(user(GUEST_ID));
        booking.setListing(bookableListing());
        booking.setMonthlyRent(RENT);
        booking.setBookingFeeAmount(new BigDecimal("50.00"));
        booking.setBookingFeeStatus(feeStatus);
        booking.setStatus(status);
        booking.setMoveInDate(Instant.now().plus(30, ChronoUnit.DAYS));
        booking.setNumberOfGuests(1);
        return booking;
    }

    private static Listing bookableListing() {
        Listing listing = new Listing();
        listing.setId(LISTING_ID);
        listing.setHost(user(HOST_ID));
        listing.setTitle("Sunny apartment");
        listing.setAddress("Kathmandu");
        listing.setRent(RENT);
        listing.setMaxGuests(4);
        listing.setActive(true);
        listing.setAvailable(true);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setAvailableFrom(Instant.now().minus(1, ChronoUnit.DAYS));
        return listing;
    }

    private static User user(UUID id) {
        User user = new User();
        user.setId(id);
        user.setEmail(id + "@example.com");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private static void authenticate(UUID userId) {
        UserPrincipal principal = new UserPrincipal(
                userId, userId + "@example.com", UserRole.USER, UserStatus.ACTIVE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
