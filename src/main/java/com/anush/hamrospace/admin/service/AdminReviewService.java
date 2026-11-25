package com.anush.hamrospace.admin.service;

import java.util.UUID;

import com.anush.hamrospace.admin.dto.request.AdminReviewFilterRequest;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.review.dto.response.ReviewResponse;

public interface AdminReviewService {

    PageResponse<ReviewResponse> getReviews(AdminReviewFilterRequest filter, PaginationRequest page);

    ReviewResponse hide(UUID id);

    ReviewResponse unhide(UUID id);

    ReviewResponse remove(UUID id);
}
