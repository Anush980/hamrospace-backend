package com.anush.hamrospace.review.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.anush.hamrospace.booking.entity.Booking;
import com.anush.hamrospace.booking.enums.BookingStatus;
import com.anush.hamrospace.booking.repository.BookingRepository;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ConflictException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.listing.repository.ListingRepository;
import com.anush.hamrospace.review.dto.request.ReviewCreateRequest;
import com.anush.hamrospace.review.dto.response.ReviewResponse;
import com.anush.hamrospace.review.entity.Review;
import com.anush.hamrospace.review.repository.ReviewRepository;
import com.anush.hamrospace.review.service.ReviewService;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    @Override
    public ReviewResponse createReview(ReviewCreateRequest request) {
        if (request == null || request.rating() == null
                || request.rating() < 1 || request.rating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }
        // if (!StringUtils.hasText(request.comment())) {
        //     throw new BadRequestException("Comment must not be blank");
        // }

        UUID currentUserId = SecurityUtil.getCurrentUserId();

        Booking booking = bookingRepository.findByIdAndGuestIdAndDeletedAtIsNull(request.bookingId(), currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ConflictException("Only completed bookings can be reviewed");
        }

        Listing listing = booking.getListing();
        if (listing == null || !request.listingId().equals(listing.getId())) {
            throw new BadRequestException("Booking does not belong to the given listing");
        }
        if (reviewRepository.existsByBookingId(request.bookingId())) {
            throw new ConflictException("A review already exists for this booking");
        }

        User author = userRepository.findById(currentUserId)
                .orElseGet(() -> userRepository.getReferenceById(currentUserId));

        Review review = new Review();
        review.setBooking(booking);
        review.setAuthor(author);
        review.setListing(listing);
        review.setTargetUser(listing.getHost());
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review saved = reviewRepository.save(review);
        recalculateListingRating(listing.getId());

        return toResponse(saved != null ? saved : review);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getListingReviews(UUID listingId, PaginationRequest page) {
        Pageable pageable = PaginationUtil.of(page);

        Specification<Review> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("listing").get("id"), listingId),
                cb.isFalse(root.get("hidden")),
                cb.isNull(root.get("deletedAt")));

        Page<Review> reviews = reviewRepository.findAll(spec, pageable);
        return PageResponse.from(reviews, this::toResponse);
    }

    @Override
    public void recalculateListingRating(UUID listingId) {
        Listing listing = listingRepository.findByIdAndDeletedAtIsNull(listingId)
                .or(() -> listingRepository.findById(listingId))
                .orElse(null);
        if (listing == null) {
            return;
        }

        long count = reviewRepository.countVisibleByListingId(listingId);
        BigDecimal average = reviewRepository.averageVisibleByListingId(listingId);

        listing.setReviewCount((int) count);
        listing.setAverageRating(average == null
                ? BigDecimal.ZERO
                : average.setScale(2, RoundingMode.HALF_UP));

        listingRepository.save(listing);
    }

    private ReviewResponse toResponse(Review review) {
        User author = review.getAuthor();
        Profile profile = author != null ? author.getProfile() : null;

        return new ReviewResponse(
                review.getId(),
                review.getListing() != null ? review.getListing().getId() : null,
                author != null ? author.getId() : null,
                profile != null ? profile.getFullName() : null,
                review.getRating(),
                review.getComment(),
                review.isHidden(),
                review.getCreatedAt());
    }
}
