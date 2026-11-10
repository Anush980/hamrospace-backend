package com.anush.hamrospace.listing.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.listing.dto.request.ListingCreateRequest;
import com.anush.hamrospace.listing.dto.request.ListingFilterRequest;
import com.anush.hamrospace.listing.dto.request.ListingUpdateRequest;
import com.anush.hamrospace.listing.dto.response.ListingDetailedResponse;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;
import com.anush.hamrospace.listing.service.ListingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {@Override
    public PageResponse<ListingSummaryResponse> getAllListings(ListingFilterRequest filterRequest,
            PaginationRequest paginationRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllListings'");
    }

    @Override
    public ListingDetailedResponse createListing(ListingCreateRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createListing'");
    }

    @Override
    public ListingDetailedResponse updateListing(ListingUpdateRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateListing'");
    }

    @Override
    public ListingSummaryResponse toggleActiveStatus(UUID listingId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toggleActiveStatus'");
    }

    @Override
    public void deleteListing(UUID listingId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteListing'");
    }
    
}
