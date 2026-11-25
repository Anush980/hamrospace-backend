package com.anush.hamrospace.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.anush.hamrospace.admin.service.impl.AdminReviewServiceImpl;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.user.UserPrincipal;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.review.dto.response.ReviewResponse;
import com.anush.hamrospace.review.entity.Review;
import com.anush.hamrospace.review.repository.ReviewRepository;
import com.anush.hamrospace.review.service.ReviewService;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceImplTest {

    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID REVIEW_ID = UUID.randomUUID();
    private static final UUID LISTING_ID = UUID.randomUUID();

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private AdminReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        authenticate(ADMIN_ID, UserRole.ADMIN);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── admin guard ──────────────────────────────────────────────────────────

    @Test
    void nonAdminCannotCallAnyAdminReviewMethodAndCannotWrite() {
        authenticate(USER_ID, UserRole.USER);

        assertThatThrownBy(() -> service.getReviews(null, new PaginationRequest()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.hide(REVIEW_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.unhide(REVIEW_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.remove(REVIEW_ID))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(reviewRepository, reviewService);
    }

    // ── reads ────────────────────────────────────────────────────────────────

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void getReviewsMapsPage() {
        PaginationRequest paginationRequest = new PaginationRequest();
        Pageable pageable = PaginationUtil.of(paginationRequest);
        Review review = review();
        when(reviewRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(review), pageable, 1));

        PageResponse<ReviewResponse> result = service.getReviews(null, paginationRequest);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(REVIEW_ID);
        assertThat(result.getContent().get(0).listingId()).isEqualTo(LISTING_ID);
        assertThat(result.getContent().get(0).rating()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ── mutations ────────────────────────────────────────────────────────────

    @Test
    void hideSetsHiddenSavesAndRecalculatesListing() {
        Review review = review();
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        ReviewResponse result = service.hide(REVIEW_ID);

        assertThat(review.isHidden()).isTrue();
        assertThat(result).isNotNull();
        assertThat(result.rating()).isEqualTo(5);
        verify(reviewRepository).save(review);
        verify(reviewService).recalculateListingRating(LISTING_ID);
    }

    @Test
    void unhideClearsHiddenSavesAndRecalculatesListing() {
        Review review = review();
        review.setHidden(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        ReviewResponse result = service.unhide(REVIEW_ID);

        assertThat(review.isHidden()).isFalse();
        assertThat(result).isNotNull();
        verify(reviewRepository).save(review);
        verify(reviewService).recalculateListingRating(LISTING_ID);
    }

    @Test
    void removeSetsDeletedAtSavesAndRecalculatesListing() {
        Review review = review();
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        service.remove(REVIEW_ID);

        assertThat(review.getDeletedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
        verify(reviewRepository).save(review);
        verify(reviewService).recalculateListingRating(LISTING_ID);
    }

    @Test
    void mutationsOnMissingReviewThrowNotFoundAndNeverWrite() {
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.hide(REVIEW_ID)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.unhide(REVIEW_ID)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.remove(REVIEW_ID)).isInstanceOf(ResourceNotFoundException.class);

        verify(reviewRepository, never()).save(any());
        verify(reviewService, never()).recalculateListingRating(any());
    }

    @Test
    void mutationOnSoftDeletedReviewThrowsNotFound() {
        Review review = review();
        review.softDelete();
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> service.remove(REVIEW_ID)).isInstanceOf(ResourceNotFoundException.class);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void mutationWithoutListingSavesButDoesNotRecalculate() {
        Review review = review();
        review.setListing(null);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        service.hide(REVIEW_ID);

        verify(reviewRepository).save(review);
        verify(reviewService, never()).recalculateListingRating(any());
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

    private static Review review() {
        Listing listing = new Listing();
        listing.setId(LISTING_ID);
        Review review = new Review();
        review.setId(REVIEW_ID);
        review.setListing(listing);
        review.setRating(5);
        review.setComment("Great place");
        return review;
    }

    private static void authenticate(UUID userId, UserRole role) {
        UserPrincipal principal = new UserPrincipal(
                userId, userId + "@example.com", role, UserStatus.ACTIVE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
