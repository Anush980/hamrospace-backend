package com.anush.hamrospace.review.service;

import java.util.UUID;

import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.review.dto.request.ReviewCreateRequest;
import com.anush.hamrospace.review.dto.response.ReviewResponse;

public interface ReviewService {

    ReviewResponse createReview(ReviewCreateRequest request);

    PageResponse<ReviewResponse> getListingReviews(UUID listingId, PaginationRequest page);

    void recalculateListingRating(UUID listingId);
}
