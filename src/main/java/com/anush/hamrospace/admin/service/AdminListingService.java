package com.anush.hamrospace.admin.service;

import java.util.UUID;

import com.anush.hamrospace.admin.dto.request.AdminListingActiveRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingFilterRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingStatusRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingVerifiedRequest;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.listing.dto.response.ListingDetailedResponse;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;

public interface AdminListingService {

    PageResponse<ListingSummaryResponse> getListings(AdminListingFilterRequest filter, PaginationRequest page);

    ListingDetailedResponse getListingById(UUID id);

    ListingDetailedResponse updateStatus(UUID id, AdminListingStatusRequest request);

    ListingDetailedResponse updateVerified(UUID id, AdminListingVerifiedRequest request);

    ListingDetailedResponse updateActive(UUID id, AdminListingActiveRequest request);
}
