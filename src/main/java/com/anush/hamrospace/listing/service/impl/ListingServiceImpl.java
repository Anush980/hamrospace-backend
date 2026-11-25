package com.anush.hamrospace.listing.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.amenity.entity.Amenity;
import com.anush.hamrospace.amenity.repository.AmenityRepository;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ConflictException;
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
import com.anush.hamrospace.listing.specification.ListingSpecification;
import com.anush.hamrospace.listing.util.SpatialUtils;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    public static final long MAX_LISTINGS_PER_USER = 50L;
    private final ListingRepository listingRepository;
    private final AmenityRepository amenityRepository;
    private final UserRepository userRepository;
    private final ListingMapper listingMapper;
    private final SpatialUtils spatialUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ListingSummaryResponse> getMyListings(ListingFilterRequest filterRequest,
            PaginationRequest paginationRequest) {

        UUID userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PaginationUtil.of(paginationRequest);

        Specification<Listing> spec = ListingSpecification.buildSpecification(userId, filterRequest);
        Page<Listing> listingsPage = listingRepository.findAll(spec, pageable);

        return PageResponse.from(listingsPage, listingMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ListingSummaryResponse> searchListings(ListingFilterRequest filterRequest,
            PaginationRequest paginationRequest) {

        Pageable pageable = PaginationUtil.of(paginationRequest);

        Specification<Listing> spec = ListingSpecification.buildSpecification(null, filterRequest);
        Page<Listing> listingsPage = listingRepository.findAll(spec, pageable);

        return PageResponse.from(listingsPage, listingMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ListingDetailedResponse getListingById(UUID listingId) {
        Listing listing = listingRepository.findByIdAndDeletedAtIsNull(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        return listingMapper.toDetailedResponse(listing);
    }

    @Override
    public ListingDetailedResponse createListing(ListingCreateRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        long currentListingCount = listingRepository.countByHostIdAndDeletedAtIsNull(userId);

        if (currentListingCount >= MAX_LISTINGS_PER_USER) {
            throw new AccessDeniedException("You can't add more than " + MAX_LISTINGS_PER_USER + " listings");
        }

        User host = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingMapper.toEntity(request);
        listing.setHost(host);
        listing.setSlug(generateUniqueSlug(request.title()));
        listing.setCoordinates(spatialUtils.createPoint(request.latitude(), request.longitude()));
        listing.setAmenities(resolveAmenities(request.amenityIds()));
        listing.setActive(true);
        listing.setAvailable(true);
        listing.setStatus(ListingStatus.PENDING);

        Listing saved = listingRepository.save(listing);

        return listingMapper.toDetailedResponse(saved);
    }

    @Override
    public ListingDetailedResponse updateListing(UUID listingId, ListingUpdateRequest request) {

        Listing existingListing = findOwnedListing(listingId, "You can only update your own listing");

        listingMapper.updateListing(request, existingListing);
        if (request.amenityIds() != null) {
            existingListing.setAmenities(resolveAmenities(request.amenityIds()));
        }
        if (request.latitude() != null && request.longitude() != null) {
            existingListing.setCoordinates(spatialUtils.createPoint(request.latitude(), request.longitude()));
        }

        Listing updated = listingRepository.save(existingListing);
        return listingMapper.toDetailedResponse(updated);
    }

    @Override
    public ListingSummaryResponse toggleActiveStatus(UUID listingId) {
        Listing existingListing = findOwnedListing(listingId, "You can only update your own listing");

        existingListing.setActive(!existingListing.isActive());
        Listing updatedListing = listingRepository.save(existingListing);
        return listingMapper.toSummaryResponse(updatedListing);
    }

    @Override
    public ListingDetailedResponse resubmitListing(UUID listingId) {
        Listing listing = findOwnedListing(listingId, "You can only resubmit your own listing");

        if (listing.getStatus() != ListingStatus.REJECTED) {
            throw new ConflictException("Only rejected listings can be resubmitted");
        }

        listing.setStatus(ListingStatus.PENDING);
        listing.setActive(true);
        listing.setRejectionReason(null);

        Listing updated = listingRepository.save(listing);
        return listingMapper.toDetailedResponse(updated);
    }

    @Override
    public void deleteListing(UUID listingId) {
        Listing existingListing = findOwnedListing(listingId, "You can only delete your own listing");

        existingListing.softDelete();
        listingRepository.save(existingListing);
    }

    private Listing findOwnedListing(UUID listingId, String accessDeniedMessage) {
        UUID userId = SecurityUtil.getCurrentUserId();
        Listing listing = listingRepository.findByIdAndDeletedAtIsNull(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.getHost().getId().equals(userId)) {
            throw new AccessDeniedException(accessDeniedMessage);
        }
        return listing;
    }

    private Set<Amenity> resolveAmenities(Set<UUID> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Amenity> amenities = amenityRepository.findAllById(amenityIds);
        if (amenities.size() != amenityIds.size()
                || amenities.stream().anyMatch(amenity -> !amenity.isActive() || amenity.isDeleted())) {
            throw new BadRequestException("One or more amenities are invalid or inactive");
        }
        return new HashSet<>(amenities);
    }

    private String generateUniqueSlug(String title) {
        String base = title == null ? "" : title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (base.isBlank()) {
            base = "listing";
        }

        String slug;
        do {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        } while (listingRepository.existsBySlug(slug));
        return slug;
    }
}
