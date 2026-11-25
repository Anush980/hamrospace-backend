package com.anush.hamrospace.admin.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.admin.dto.request.AdminReviewFilterRequest;
import com.anush.hamrospace.admin.service.AdminReviewService;
import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.review.dto.response.ReviewResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION+ApiPath.ADMIN+ApiPath.REVIEW)
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping("")
    @Operation(summary = "List reviews for moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ReviewResponse>> getReviews(
            @Valid AdminReviewFilterRequest filter,
            @Valid PaginationRequest page) {
        return ApiResponse.ok("Reviews fetched successfully", adminReviewService.getReviews(filter, page));
    }

    @PatchMapping(ApiPath.ID + "/hide")
    @Operation(summary = "Hide a review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReviewResponse> hide(@PathVariable("id") UUID id) {
        return ApiResponse.ok("Review hidden successfully", adminReviewService.hide(id));
    }

    @PatchMapping(ApiPath.ID + "/unhide")
    @Operation(summary = "Unhide a review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReviewResponse> unhide(@PathVariable("id") UUID id) {
        return ApiResponse.ok("Review unhidden successfully", adminReviewService.unhide(id));
    }

    @DeleteMapping(ApiPath.ID)
    @Operation(summary = "Remove a review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReviewResponse> remove(@PathVariable("id") UUID id) {
        return ApiResponse.ok("Review removed successfully", adminReviewService.remove(id));
    }
}
