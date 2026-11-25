package com.anush.hamrospace.admin.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.admin.dto.request.AdminListingActiveRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingFilterRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingStatusRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingVerifiedRequest;
import com.anush.hamrospace.admin.service.AdminListingService;
import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.listing.dto.response.ListingDetailedResponse;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION+ApiPath.ADMIN+ApiPath.LISTING) 
@PreAuthorize("hasRole('ADMIN')")
public class AdminListingController {

    private final AdminListingService adminListingService;

    @GetMapping("")
    @Operation(summary = "List all listings for moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ListingSummaryResponse>> getListings(
            @Valid AdminListingFilterRequest filter,
            @Valid PaginationRequest page) {
        return ApiResponse.ok("Listings fetched successfully", adminListingService.getListings(filter, page));
    }

    @GetMapping(ApiPath.ID)
    @Operation(summary = "Get listing details for moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ListingDetailedResponse> getListingById(@PathVariable("id") UUID id) {
        return ApiResponse.ok("Listing fetched successfully", adminListingService.getListingById(id));
    }

    @PatchMapping(ApiPath.ID + "/status")
    @Operation(summary = "Update listing moderation status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ListingDetailedResponse> updateStatus(@PathVariable("id") UUID id,
            @Valid @RequestBody AdminListingStatusRequest request) {
        return ApiResponse.ok("Listing status updated successfully", adminListingService.updateStatus(id, request));
    }

    @PatchMapping(ApiPath.ID + "/verified")
    @Operation(summary = "Update listing verified flag")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ListingDetailedResponse> updateVerified(@PathVariable("id") UUID id,
            @Valid @RequestBody AdminListingVerifiedRequest request) {
        return ApiResponse.ok("Listing verification updated successfully",
                adminListingService.updateVerified(id, request));
    }

    @PatchMapping(ApiPath.ID + "/active")
    @Operation(summary = "Update listing active flag")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ListingDetailedResponse> updateActive(@PathVariable("id") UUID id,
            @Valid @RequestBody AdminListingActiveRequest request) {
        return ApiResponse.ok("Listing active flag updated successfully",
                adminListingService.updateActive(id, request));
    }
}
