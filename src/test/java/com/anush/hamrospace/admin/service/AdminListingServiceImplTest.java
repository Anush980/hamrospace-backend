package com.anush.hamrospace.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.anush.hamrospace.admin.dto.request.AdminListingActiveRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingStatusRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingVerifiedRequest;
import com.anush.hamrospace.admin.service.impl.AdminListingServiceImpl;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ConflictException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.user.UserPrincipal;
import com.anush.hamrospace.listing.dto.response.ListingDetailedResponse;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.listing.enums.ListingStatus;
import com.anush.hamrospace.listing.mapper.ListingMapper;
import com.anush.hamrospace.listing.repository.ListingRepository;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;

@ExtendWith(MockitoExtension.class)
class AdminListingServiceImplTest {

    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID LISTING_ID = UUID.randomUUID();

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private ListingMapper listingMapper;

    @InjectMocks
    private AdminListingServiceImpl service;

    @BeforeEach
    void setUp() {
        authenticate(ADMIN_ID, UserRole.ADMIN);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── admin guard ──────────────────────────────────────────────────────────

    @Test
    void nonAdminCannotCallAnyAdminListingMethodAndCannotWrite() {
        authenticate(USER_ID, UserRole.USER);

        assertThatThrownBy(() -> service.getListings(null, new PaginationRequest()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.getListingById(LISTING_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.updateStatus(LISTING_ID, new AdminListingStatusRequest(ListingStatus.ACTIVE, null)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.updateVerified(LISTING_ID, new AdminListingVerifiedRequest(true)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.updateActive(LISTING_ID, new AdminListingActiveRequest(false)))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(listingRepository, listingMapper);
    }

    // ── reads ────────────────────────────────────────────────────────────────

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void getListingsMapsPageAndUsesPagination() {
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setPageNumber(0);
        paginationRequest.setPageSize(5);
        Pageable pageable = PaginationUtil.of(paginationRequest);
        Listing listing = pendingListing();
        ListingSummaryResponse summary = mock(ListingSummaryResponse.class);
        when(listingRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(listing), pageable, 1));
        when(listingMapper.toSummaryResponse(listing)).thenReturn(summary);

        PageResponse<ListingSummaryResponse> result = service.getListings(null, paginationRequest);

        assertThat(result.getContent()).containsExactly(summary);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
    }

    @Test
    void getListingByIdMapsExistingListing() {
        Listing listing = pendingListing();
        ListingDetailedResponse expected = mock(ListingDetailedResponse.class);
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        when(listingMapper.toDetailedResponse(listing)).thenReturn(expected);

        assertThat(service.getListingById(LISTING_ID)).isSameAs(expected);
    }

    @Test
    void getListingByIdFailsWhenMissing() {
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getListingById(LISTING_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateStatus ─────────────────────────────────────────────────────────

    @Test
    void updateStatusToActiveApprovesAndClearsRejection() {
        Listing listing = pendingListing();
        listing.setActive(false);
        listing.setRejectionReason("Bad address");
        ListingDetailedResponse expected = mock(ListingDetailedResponse.class);
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        saveReturnsArgument();
        when(listingMapper.toDetailedResponse(listing)).thenReturn(expected);

        ListingDetailedResponse result = service.updateStatus(
                LISTING_ID, new AdminListingStatusRequest(ListingStatus.ACTIVE, null));

        assertThat(result).isSameAs(expected);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(listing.isActive()).isTrue();
        assertThat(listing.getRejectionReason()).isNull();
        verify(listingRepository).save(listing);
    }

    @Test
    void updateStatusToRejectedWithoutReasonIsBadRequestAndDoesNotSave() {
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID))
                .thenReturn(Optional.of(pendingListing()));

        assertThatThrownBy(() -> service.updateStatus(
                LISTING_ID, new AdminListingStatusRequest(ListingStatus.REJECTED, "  ")))
                .isInstanceOf(BadRequestException.class);
        verify(listingRepository, never()).save(any());
    }

    @Test
    void updateStatusToRejectedWithReasonDeactivatesListing() {
        Listing listing = pendingListing();
        ListingDetailedResponse expected = mock(ListingDetailedResponse.class);
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        saveReturnsArgument();
        when(listingMapper.toDetailedResponse(listing)).thenReturn(expected);

        service.updateStatus(LISTING_ID, new AdminListingStatusRequest(ListingStatus.REJECTED, "Fix address"));

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.REJECTED);
        assertThat(listing.isActive()).isFalse();
        assertThat(listing.getRejectionReason()).isEqualTo("Fix address");
        verify(listingRepository).save(listing);
    }

    @Test
    void updateStatusToRentedFromPendingIsConflict() {
        Listing listing = pendingListing();
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> service.updateStatus(
                LISTING_ID, new AdminListingStatusRequest(ListingStatus.RENTED, null)))
                .isInstanceOf(ConflictException.class);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.PENDING);
        verify(listingRepository, never()).save(any());
    }

    @Test
    void updateStatusToRentedFromActiveSucceeds() {
        Listing listing = pendingListing();
        listing.setStatus(ListingStatus.ACTIVE);
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        saveReturnsArgument();

        service.updateStatus(LISTING_ID, new AdminListingStatusRequest(ListingStatus.RENTED, null));

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.RENTED);
        verify(listingRepository).save(listing);
    }

    @Test
    void updateStatusToPendingClearsRejectionReason() {
        Listing listing = pendingListing();
        listing.setRejectionReason("Old reason");
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        saveReturnsArgument();

        service.updateStatus(LISTING_ID, new AdminListingStatusRequest(ListingStatus.PENDING, null));

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.PENDING);
        assertThat(listing.getRejectionReason()).isNull();
        verify(listingRepository).save(listing);
    }

    @Test
    void updateStatusForMissingListingThrowsNotFound() {
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(
                LISTING_ID, new AdminListingStatusRequest(ListingStatus.ACTIVE, null)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(listingRepository, never()).save(any());
    }

    @Test
    void updateStatusWithNullRequestIsBadRequest() {
        assertThatThrownBy(() -> service.updateStatus(LISTING_ID, null))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(listingRepository);
    }

    // ── updateVerified / updateActive ────────────────────────────────────────

    @Test
    void updateVerifiedBindsFlagAndSaves() {
        Listing listing = pendingListing();
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        saveReturnsArgument();

        service.updateVerified(LISTING_ID, new AdminListingVerifiedRequest(true));

        assertThat(listing.isVerified()).isTrue();
        verify(listingRepository).save(listing);
    }

    @Test
    void updateVerifiedWithNullFlagIsBadRequest() {
        assertThatThrownBy(() -> service.updateVerified(LISTING_ID, new AdminListingVerifiedRequest(null)))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.updateVerified(LISTING_ID, null))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(listingRepository);
    }

    @Test
    void updateActiveBindsFlagAndSaves() {
        Listing listing = pendingListing();
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        saveReturnsArgument();

        service.updateActive(LISTING_ID, new AdminListingActiveRequest(false));

        assertThat(listing.isActive()).isFalse();
        verify(listingRepository).save(listing);
    }

    @Test
    void updateActiveWithNullFlagIsBadRequest() {
        assertThatThrownBy(() -> service.updateActive(LISTING_ID, new AdminListingActiveRequest(null)))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.updateActive(LISTING_ID, null))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(listingRepository);
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

    private void saveReturnsArgument() {
        when(listingRepository.save(any(Listing.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static Listing pendingListing() {
        Listing listing = new Listing();
        listing.setId(LISTING_ID);
        listing.setTitle("Sunny Apartment");
        listing.setStatus(ListingStatus.PENDING);
        listing.setActive(true);
        listing.setVerified(false);
        return listing;
    }

    private static void authenticate(UUID userId, UserRole role) {
        UserPrincipal principal = new UserPrincipal(
                userId, userId + "@example.com", role, UserStatus.ACTIVE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
