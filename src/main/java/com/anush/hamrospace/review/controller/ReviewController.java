package com.anush.hamrospace.review.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.review.dto.request.ReviewCreateRequest;
import com.anush.hamrospace.review.dto.response.ReviewResponse;
import com.anush.hamrospace.review.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION +ApiPath.REVIEW)
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping()
    @Operation(summary = "Create a review for a completed booking")
    public ApiResponse<ReviewResponse> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        return ApiResponse.ok("Review created successfully", reviewService.createReview(request));
    }

    @GetMapping("/listing/{listingId}")
    @Operation(summary = "Get visible reviews for a listing")
    public ApiResponse<PageResponse<ReviewResponse>> getListingReviews(
            @PathVariable("listingId") UUID listingId,
            @Valid PaginationRequest page) {
        return ApiResponse.ok("Reviews fetched successfully",
                reviewService.getListingReviews(listingId, page));
    }
}
