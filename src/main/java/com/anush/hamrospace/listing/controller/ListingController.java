package com.anush.hamrospace.listing.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.listing.dto.request.ListingCreateRequest;
import com.anush.hamrospace.listing.dto.request.ListingFilterRequest;
import com.anush.hamrospace.listing.dto.request.ListingUpdateRequest;
import com.anush.hamrospace.listing.dto.response.ListingDetailedResponse;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;
import com.anush.hamrospace.listing.service.ListingService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION + ApiPath.LISTING)
public class ListingController {

    private final ListingService listingService;

    @GetMapping(ApiPath.ME)
    @Operation(summary = "Get my listings")
    public ApiResponse<PageResponse<ListingSummaryResponse>> getMyListings(
            @Valid ListingFilterRequest filterRequest,
            @Valid PaginationRequest paginationRequest) {
        return ApiResponse.ok("Listings fetched successfully",
                listingService.getMyListings(filterRequest, paginationRequest));
    }

    @PostMapping("/create")
    @Operation(summary = "Create new listing")
    public ApiResponse<ListingDetailedResponse> createListing(@Valid @RequestBody ListingCreateRequest request) {
        return ApiResponse.ok("Listing created successfully", listingService.createListing(request));
    }

    @PatchMapping(ApiPath.ID)
    @Operation(summary = "Update existing listing")
    public ApiResponse<ListingDetailedResponse> updateListing(@PathVariable("id") UUID listingId,
            @Valid @RequestBody ListingUpdateRequest request) {
        return ApiResponse.ok("Listing updated successfully", listingService.updateListing(listingId, request));
    }

    @PatchMapping(ApiPath.ID + "/toggle-active")
    @Operation(summary = "Toggle listing active status")
    public ApiResponse<ListingSummaryResponse> toggleActiveStatus(@PathVariable("id") UUID listingId) {
        return ApiResponse.ok("Listing active status toggled successfully",
                listingService.toggleActiveStatus(listingId));
    }

    @PostMapping(ApiPath.ID + "/resubmit")
    @Operation(summary = "Resubmit a rejected listing for review (host only)")
    public ApiResponse<ListingDetailedResponse> resubmitListing(@PathVariable("id") UUID listingId) {
        return ApiResponse.ok("Listing resubmitted successfully", listingService.resubmitListing(listingId));
    }

    @DeleteMapping(ApiPath.ID)
    @Operation(summary = "Delete listing")
    public ApiResponse<Void> deleteListing(@PathVariable("id") UUID listingId) {
        listingService.deleteListing(listingId);
        return ApiResponse.ok("Listing deleted successfully");
    }

    // @GetMapping({ "", "/" })
    @GetMapping()
    @Operation(summary = "Search public listings")
    public ApiResponse<PageResponse<ListingSummaryResponse>> searchListings(
            @Valid ListingFilterRequest filterRequest,
            @Valid PaginationRequest paginationRequest) {
        return ApiResponse.ok("Listings fetched successfully",
                listingService.searchListings(filterRequest, paginationRequest));
    }

    @GetMapping(ApiPath.ID)
    @Operation(summary = "Get listing by id")
    public ApiResponse<ListingDetailedResponse> getListingById(@PathVariable("id") UUID listingId) {
        return ApiResponse.ok("Listing fetched successfully", listingService.getListingById(listingId));
    }
}
