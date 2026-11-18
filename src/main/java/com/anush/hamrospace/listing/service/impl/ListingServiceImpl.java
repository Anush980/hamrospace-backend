package com.anush.hamrospace.listing.service.impl;

import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.listing.dto.request.ListingCreateRequest;
import com.anush.hamrospace.listing.dto.request.ListingFilterRequest;
import com.anush.hamrospace.listing.dto.request.ListingUpdateRequest;
import com.anush.hamrospace.listing.dto.response.ListingDetailedResponse;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.listing.enums.ListingStatus;
import com.anush.hamrospace.listing.mapper.ListingMapper;
import com.anush.hamrospace.listing.repository.ListingRepository;
import com.anush.hamrospace.listing.service.ListingService;
import com.anush.hamrospace.listing.util.SpatialUtils;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    public static final long MAX_LISTINGS_PER_USER = 50L;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ListingMapper listingMapper;
    private final SpatialUtils spatialUtils;

    @Override
    public PageResponse<ListingSummaryResponse> getMyListings(ListingFilterRequest filterRequest,
            PaginationRequest paginationRequest) {

        UUID userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PaginationUtil.of(paginationRequest);
        // Specification<Listing> spec =
        throw new UnsupportedOperationException("Unimplemented method 'createListing'");

    }

    @Override
    public ListingDetailedResponse createListing(ListingCreateRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        long currentListingCount = listingRepository.countByUserIdAndDeletedAtIsNull(userId);

        if (currentListingCount > MAX_LISTINGS_PER_USER) {
            throw new AccessDeniedException("You can't add more than " + MAX_LISTINGS_PER_USER + " listings");
        }
        Listing listing = listingMapper.toEntity(request);

        User host = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        listing.setHost(host);
        Point coordinates = spatialUtils.createPoint(request.latitude(), request.longitude());
        listing.setCoordinates(coordinates);

        listing.setActive(true);
        listing.setAvailable(true);
        listing.setStatus(ListingStatus.PENDING);
        Listing saved = listingRepository.save(listing);

        return listingMapper.toDetailedResponse(saved);
    }

    @Override
    public ListingDetailedResponse updateListing(UUID listingId, ListingUpdateRequest request) {

        UUID userId = SecurityUtil.getCurrentUserId();
        Listing existingListing = listingRepository.findByIdAndDeletedAtIsNull(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!existingListing.getHost().getId().equals(userId)) {
            throw new AccessDeniedException("You can only update your own listing");
        }
        listingMapper.updateListing(request, existingListing);
        if (request.latitude() != null && request.longitude() != null) {
            existingListing.setCoordinates(spatialUtils.createPoint(request.latitude(), request.longitude()));
        }

        Listing updated = listingRepository.save(existingListing);
        return listingMapper.toDetailedResponse(updated);
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
