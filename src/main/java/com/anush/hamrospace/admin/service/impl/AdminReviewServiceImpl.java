package com.anush.hamrospace.admin.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.admin.dto.request.AdminReviewFilterRequest;
import com.anush.hamrospace.admin.service.AdminReviewService;
import com.anush.hamrospace.admin.specification.AdminReviewSpecification;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.review.dto.response.ReviewResponse;
import com.anush.hamrospace.review.entity.Review;
import com.anush.hamrospace.review.repository.ReviewRepository;
import com.anush.hamrospace.review.service.ReviewService;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getReviews(AdminReviewFilterRequest filter, PaginationRequest page) {
        requireAdmin();

        Pageable pageable = PaginationUtil.of(page);
        Specification<Review> spec = AdminReviewSpecification.buildSpecification(filter);
        Page<Review> reviews = reviewRepository.findAll(spec, pageable);

        return PageResponse.from(reviews, this::toResponse);
    }

    @Override
    public ReviewResponse hide(UUID id) {
        requireAdmin();

        Review review = findReview(id);
        review.setHidden(true);

        return saveAndRecalculate(review);
    }

    @Override
    public ReviewResponse unhide(UUID id) {
        requireAdmin();

        Review review = findReview(id);
        review.setHidden(false);

        return saveAndRecalculate(review);
    }

    @Override
    public ReviewResponse remove(UUID id) {
        requireAdmin();

        Review review = findReview(id);
        review.setDeletedAt(Instant.now());

        return saveAndRecalculate(review);
    }

    private Review findReview(UUID id) {
        return reviewRepository.findById(id)
                .filter(review -> review.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    private ReviewResponse saveAndRecalculate(Review review) {
        UUID listingId = review.getListing() != null ? review.getListing().getId() : null;
        Review saved = reviewRepository.save(review);

        if (listingId != null) {
            reviewService.recalculateListingRating(listingId);
        }

        return toResponse(saved != null ? saved : review);
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

    private void requireAdmin() {
        if (SecurityUtil.getCurrentUser().getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}
