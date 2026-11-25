package com.anush.hamrospace.admin.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.anush.hamrospace.admin.dto.request.AdminListingActiveRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingFilterRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingStatusRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingVerifiedRequest;
import com.anush.hamrospace.admin.service.AdminListingService;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ConflictException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.listing.dto.response.ListingDetailedResponse;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.listing.enums.ListingStatus;
import com.anush.hamrospace.listing.mapper.ListingMapper;
import com.anush.hamrospace.listing.repository.ListingRepository;
import com.anush.hamrospace.user.enums.UserRole;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminListingServiceImpl implements AdminListingService {

    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ListingSummaryResponse> getListings(AdminListingFilterRequest filter,
            PaginationRequest page) {
        requireAdmin();

        Pageable pageable = PaginationUtil.of(page);
        Specification<Listing> spec = buildSpecification(filter);
        Page<Listing> listings = listingRepository.findAll(spec, pageable);

        return PageResponse.from(listings, listingMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ListingDetailedResponse getListingById(UUID id) {
        requireAdmin();

        return listingMapper.toDetailedResponse(findListing(id));
    }

    @Override
    public ListingDetailedResponse updateStatus(UUID id, AdminListingStatusRequest request) {
        requireAdmin();

        if (request == null || request.status() == null) {
            throw new BadRequestException("Status is required");
        }

        Listing listing = findListing(id);
        ListingStatus target = request.status();

        switch (target) {
            case ACTIVE -> {
                listing.setStatus(ListingStatus.ACTIVE);
                listing.setRejectionReason(null);
                listing.setActive(true);
            }
            case REJECTED -> {
                if (!StringUtils.hasText(request.reason())) {
                    throw new BadRequestException("A rejection reason is required");
                }
                listing.setStatus(ListingStatus.REJECTED);
                listing.setRejectionReason(request.reason());
                listing.setActive(false);
            }
            case PENDING -> {
                listing.setStatus(ListingStatus.PENDING);
                listing.setRejectionReason(null);
            }
            case RENTED -> {
                if (listing.getStatus() != ListingStatus.ACTIVE) {
                    throw new ConflictException("Only active listings can be marked as rented");
                }
                listing.setStatus(ListingStatus.RENTED);
            }
        }

        return listingMapper.toDetailedResponse(listingRepository.save(listing));
    }

    @Override
    public ListingDetailedResponse updateVerified(UUID id, AdminListingVerifiedRequest request) {
        requireAdmin();

        if (request == null || request.verified() == null) {
            throw new BadRequestException("Verified flag is required");
        }

        Listing listing = findListing(id);
        listing.setVerified(request.verified());

        return listingMapper.toDetailedResponse(listingRepository.save(listing));
    }

    @Override
    public ListingDetailedResponse updateActive(UUID id, AdminListingActiveRequest request) {
        requireAdmin();

        if (request == null || request.active() == null) {
            throw new BadRequestException("Active flag is required");
        }

        Listing listing = findListing(id);
        listing.setActive(request.active());

        return listingMapper.toDetailedResponse(listingRepository.save(listing));
    }

    private Listing findListing(UUID id) {
        return listingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
    }

    private Specification<Listing> buildSpecification(AdminListingFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filter != null) {
                if (filter.status() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.status()));
                }
                if (StringUtils.hasText(filter.keyword())) {
                    String pattern = "%" + filter.keyword().trim().toLowerCase(Locale.ROOT) + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("title")), pattern),
                            cb.like(cb.lower(root.get("address")), pattern)));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void requireAdmin() {
        if (SecurityUtil.getCurrentUser().getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}
