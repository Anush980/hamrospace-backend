package com.anush.hamrospace.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.anush.hamrospace.booking.entity.Booking;
import com.anush.hamrospace.booking.enums.BookingStatus;
import com.anush.hamrospace.booking.repository.BookingRepository;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ConflictException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.user.UserPrincipal;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.listing.repository.ListingRepository;
import com.anush.hamrospace.review.dto.request.ReviewCreateRequest;
import com.anush.hamrospace.review.dto.response.ReviewResponse;
import com.anush.hamrospace.review.entity.Review;
import com.anush.hamrospace.review.repository.ReviewRepository;
import com.anush.hamrospace.review.service.impl.ReviewServiceImpl;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;
import com.anush.hamrospace.user.repository.UserRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    private static final UUID GUEST_ID = UUID.randomUUID();
    private static final UUID OTHER_GUEST_ID = UUID.randomUUID();
    private static final UUID BOOKING_ID = UUID.randomUUID();
    private static final UUID LISTING_ID = UUID.randomUUID();
    private static final UUID REVIEW_ID = UUID.randomUUID();

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private ReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        authenticate(GUEST_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── createReview ─────────────────────────────────────────────────────────

    @Test
    void createReviewForCompletedBookingSucceedsAndRecalculatesStats() {
        Listing listing = listing();
        Booking booking = booking(BookingStatus.COMPLETED, listing);
        User author = user(GUEST_ID);
        when(bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID))
                .thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(BOOKING_ID)).thenReturn(false);
        when(userRepository.findById(GUEST_ID)).thenReturn(Optional.of(author));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(REVIEW_ID);
            return review;
        });
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        when(reviewRepository.countVisibleByListingId(LISTING_ID)).thenReturn(2L);
        when(reviewRepository.averageVisibleByListingId(LISTING_ID)).thenReturn(new BigDecimal("4.555"));

        ReviewResponse result = service.createReview(validRequest());

        assertThat(result.id()).isEqualTo(REVIEW_ID);
        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.comment()).isEqualTo("Great place");

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review saved = reviewCaptor.getValue();
        assertThat(saved.getBooking()).isSameAs(booking);
        assertThat(saved.getAuthor()).isSameAs(author);
        assertThat(saved.getListing()).isSameAs(listing);
        assertThat(saved.getTargetUser()).isSameAs(listing.getHost());

        assertThat(listing.getReviewCount()).isEqualTo(2);
        assertThat(listing.getAverageRating()).isEqualByComparingTo("4.56");
        verify(listingRepository).save(listing);
    }

    @Test
    void createReviewForBookingOfAnotherGuestThrowsNotFound() {
        when(bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createReview(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(bookingRepository).findByIdAndGuestIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID);
        verify(reviewRepository, never()).save(any());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void createReviewForNonCompletedBookingThrowsConflict() {
        when(bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID))
                .thenReturn(Optional.of(booking(BookingStatus.CONFIRMED, listing())));

        assertThatThrownBy(() -> service.createReview(validRequest()))
                .isInstanceOf(ConflictException.class);
        verify(reviewRepository, never()).save(any());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void createReviewForWrongListingThrowsBadRequest() {
        Listing otherListing = new Listing();
        otherListing.setId(UUID.randomUUID());
        when(bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID))
                .thenReturn(Optional.of(booking(BookingStatus.COMPLETED, otherListing)));

        assertThatThrownBy(() -> service.createReview(validRequest()))
                .isInstanceOf(BadRequestException.class);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createDuplicateReviewThrowsConflict() {
        when(bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(BOOKING_ID, GUEST_ID))
                .thenReturn(Optional.of(booking(BookingStatus.COMPLETED, listing())));
        when(reviewRepository.existsByBookingId(BOOKING_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.createReview(validRequest()))
                .isInstanceOf(ConflictException.class);
        verify(reviewRepository, never()).save(any());
        verify(listingRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 6 })
    void createReviewRejectsRatingOutsideRange(int rating) {
        ReviewCreateRequest request = new ReviewCreateRequest(BOOKING_ID, LISTING_ID, rating, "Great place");

        assertThatThrownBy(() -> service.createReview(request))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(bookingRepository, reviewRepository, userRepository, listingRepository);
    }

    @Test
    void createReviewRejectsNullRating() {
        ReviewCreateRequest request = new ReviewCreateRequest(BOOKING_ID, LISTING_ID, null, "Great place");

        assertThatThrownBy(() -> service.createReview(request))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(bookingRepository, reviewRepository, userRepository, listingRepository);
    }

    @Test
    void createReviewRejectsBlankComment() {
        ReviewCreateRequest request = new ReviewCreateRequest(BOOKING_ID, LISTING_ID, 5, "   ");

        assertThatThrownBy(() -> service.createReview(request))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(bookingRepository, reviewRepository, userRepository, listingRepository);
    }

    @Test
    void createReviewRejectsNullRequest() {
        assertThatThrownBy(() -> service.createReview(null))
                .isInstanceOf(BadRequestException.class);
    }

    // ── getListingReviews ────────────────────────────────────────────────────

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void getListingReviewsFiltersHiddenAndDeletedAndMapsResults() {
        PaginationRequest paginationRequest = new PaginationRequest();
        Pageable pageable = PaginationUtil.of(paginationRequest);
        Review review = new Review();
        review.setId(REVIEW_ID);
        review.setRating(4);
        review.setComment("Nice");
        when(reviewRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(review), pageable, 1));

        PageResponse<ReviewResponse> result = service.getListingReviews(LISTING_ID, paginationRequest);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(REVIEW_ID);
        assertThat(result.getContent().get(0).rating()).isEqualTo(4);
        assertThat(result.getTotalElements()).isEqualTo(1);

        ArgumentCaptor<Specification> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(reviewRepository).findAll(specCaptor.capture(), eq(pageable));

        Root root = mock(Root.class);
        Path listingPath = mock(Path.class);
        Path listingIdPath = mock(Path.class);
        Path hiddenPath = mock(Path.class);
        Path deletedPath = mock(Path.class);
        when(root.get("listing")).thenReturn(listingPath);
        when(listingPath.get("id")).thenReturn(listingIdPath);
        when(root.get("hidden")).thenReturn(hiddenPath);
        when(root.get("deletedAt")).thenReturn(deletedPath);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        specCaptor.getValue().toPredicate(root, null, cb);
        verify(cb).equal(listingIdPath, LISTING_ID);
        verify(cb).isFalse(hiddenPath);
        verify(cb).isNull(deletedPath);
    }

    // ── recalculateListingRating ─────────────────────────────────────────────

    @Test
    void recalculateListingRatingUsesZeroWhenNoVisibleReviews() {
        Listing listing = listing();
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        when(reviewRepository.countVisibleByListingId(LISTING_ID)).thenReturn(0L);
        when(reviewRepository.averageVisibleByListingId(LISTING_ID)).thenReturn(null);

        service.recalculateListingRating(LISTING_ID);

        assertThat(listing.getReviewCount()).isZero();
        assertThat(listing.getAverageRating()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(listingRepository).save(listing);
    }

    @Test
    void recalculateListingRatingIgnoresMissingListing() {
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.empty());
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

        service.recalculateListingRating(LISTING_ID);

        verify(reviewRepository, never()).countVisibleByListingId(any());
        verify(listingRepository, never()).save(any());
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

    private static ReviewCreateRequest validRequest() {
        return new ReviewCreateRequest(BOOKING_ID, LISTING_ID, 5, "Great place");
    }

    private static Booking booking(BookingStatus status, Listing listing) {
        Booking booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setStatus(status);
        booking.setListing(listing);
        booking.setGuest(user(GUEST_ID));
        return booking;
    }

    private static Listing listing() {
        Listing listing = new Listing();
        listing.setId(LISTING_ID);
        listing.setTitle("Sunny Apartment");
        listing.setHost(user(UUID.randomUUID()));
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
