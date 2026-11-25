package com.anush.hamrospace.listing.service;

import java.util.UUID;

import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.listing.dto.request.ListingCreateRequest;
import com.anush.hamrospace.listing.dto.request.ListingFilterRequest;
import com.anush.hamrospace.listing.dto.request.ListingUpdateRequest;
import com.anush.hamrospace.listing.dto.response.ListingDetailedResponse;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;

public interface ListingService {
    
    PageResponse<ListingSummaryResponse> getMyListings(ListingFilterRequest filterRequest,PaginationRequest paginationRequest);

    PageResponse<ListingSummaryResponse> searchListings(ListingFilterRequest filterRequest,
            PaginationRequest paginationRequest);

    ListingDetailedResponse getListingById(UUID listingId);

    ListingDetailedResponse createListing(ListingCreateRequest request);

    ListingDetailedResponse updateListing(UUID listingId,ListingUpdateRequest request);

    ListingSummaryResponse toggleActiveStatus(UUID listingId);

    ListingDetailedResponse resubmitListing(UUID listingId);

    void deleteListing(UUID listingId);


}
