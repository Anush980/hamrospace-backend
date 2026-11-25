package com.anush.hamrospace.listing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.anush.hamrospace.amenity.entity.Amenity;
import com.anush.hamrospace.amenity.repository.AmenityRepository;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.user.UserPrincipal;
import com.anush.hamrospace.listing.dto.request.ListingCreateRequest;
import com.anush.hamrospace.listing.dto.request.ListingFilterRequest;
import com.anush.hamrospace.listing.dto.request.ListingUpdateRequest;
import com.anush.hamrospace.listing.dto.response.ListingDetailedResponse;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.listing.enums.ListingStatus;
import com.anush.hamrospace.listing.enums.PricingType;
import com.anush.hamrospace.listing.enums.PropertyType;
import com.anush.hamrospace.listing.enums.RoomType;
import com.anush.hamrospace.listing.mapper.ListingMapper;
import com.anush.hamrospace.listing.repository.ListingRepository;
import com.anush.hamrospace.listing.service.impl.ListingServiceImpl;
import com.anush.hamrospace.listing.util.SpatialUtils;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;
import com.anush.hamrospace.user.repository.UserRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class ListingServiceImplTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID OTHER_USER_ID = UUID.randomUUID();
    private static final UUID LISTING_ID = UUID.randomUUID();
    private static final UUID AMENITY_ID = UUID.randomUUID();

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private AmenityRepository amenityRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ListingMapper listingMapper;
    @Mock
    private SpatialUtils spatialUtils;

    @InjectMocks
    private ListingServiceImpl service;

    @BeforeEach
    void setUp() {
        authenticate(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── createListing ────────────────────────────────────────────────────────

    @Test
    void createListingUsesJwtUserAndPersistsDefaults() {
        ListingCreateRequest request = createRequest(Set.of(AMENITY_ID));
        Point point = mock(Point.class);
        Amenity amenity = activeAmenity();
        ListingDetailedResponse expected = mock(ListingDetailedResponse.class);

        when(listingRepository.countByHostIdAndDeletedAtIsNull(USER_ID)).thenReturn(0L);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID)));
        when(listingMapper.toEntity(request)).thenReturn(new Listing());
        when(spatialUtils.createPoint(27.7, 85.3)).thenReturn(point);
        when(amenityRepository.findAllById(Set.of(AMENITY_ID))).thenReturn(List.of(amenity));
        saveReturnsArgument();
        when(listingMapper.toDetailedResponse(any(Listing.class))).thenReturn(expected);

        assertThat(service.createListing(request)).isSameAs(expected);

        ArgumentCaptor<Listing> captor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository).save(captor.capture());
        Listing saved = captor.getValue();
        assertThat(saved.getHost().getId()).isEqualTo(USER_ID);
        assertThat(saved.getSlug()).matches("^[a-z0-9-]+-[0-9a-f]{8}$");
        assertThat(saved.getCoordinates()).isSameAs(point);
        assertThat(saved.getAmenities()).containsExactly(amenity);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.isAvailable()).isTrue();
        assertThat(saved.getStatus()).isEqualTo(ListingStatus.PENDING);
    }

    @Test
    void createListingAtQuotaLimitIsDenied() {
        when(listingRepository.countByHostIdAndDeletedAtIsNull(USER_ID))
                .thenReturn(ListingServiceImpl.MAX_LISTINGS_PER_USER);

        assertThatThrownBy(() -> service.createListing(createRequest(Set.of())))
                .isInstanceOf(AccessDeniedException.class);
        verify(listingRepository, never()).save(any());
    }

    @Test
    void createListingJustBelowQuotaSucceeds() {
        ListingDetailedResponse expected = mock(ListingDetailedResponse.class);
        when(listingRepository.countByHostIdAndDeletedAtIsNull(USER_ID))
                .thenReturn(ListingServiceImpl.MAX_LISTINGS_PER_USER - 1);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID)));
        when(listingMapper.toEntity(any(ListingCreateRequest.class))).thenReturn(new Listing());
        saveReturnsArgument();
        when(listingMapper.toDetailedResponse(any(Listing.class))).thenReturn(expected);

        assertThat(service.createListing(createRequest(Set.of()))).isSameAs(expected);
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void createListingRejectsUnknownAmenity() {
        when(listingRepository.countByHostIdAndDeletedAtIsNull(USER_ID)).thenReturn(0L);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID)));
        when(listingMapper.toEntity(any(ListingCreateRequest.class))).thenReturn(new Listing());
        when(amenityRepository.findAllById(Set.of(AMENITY_ID))).thenReturn(List.of());

        assertThatThrownBy(() -> service.createListing(createRequest(Set.of(AMENITY_ID))))
                .isInstanceOf(BadRequestException.class);
        verify(listingRepository, never()).save(any());
    }

    @Test
    void createListingRejectsInactiveAmenity() {
        Amenity inactive = activeAmenity();
        inactive.setActive(false);
        when(listingRepository.countByHostIdAndDeletedAtIsNull(USER_ID)).thenReturn(0L);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID)));
        when(listingMapper.toEntity(any(ListingCreateRequest.class))).thenReturn(new Listing());
        when(amenityRepository.findAllById(Set.of(AMENITY_ID))).thenReturn(List.of(inactive));

        assertThatThrownBy(() -> service.createListing(createRequest(Set.of(AMENITY_ID))))
                .isInstanceOf(BadRequestException.class);
        verify(listingRepository, never()).save(any());
    }

    @Test
    void createListingRejectsDeletedAmenity() {
        Amenity deleted = activeAmenity();
        deleted.softDelete();
        when(listingRepository.countByHostIdAndDeletedAtIsNull(USER_ID)).thenReturn(0L);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID)));
        when(listingMapper.toEntity(any(ListingCreateRequest.class))).thenReturn(new Listing());
        when(amenityRepository.findAllById(Set.of(AMENITY_ID))).thenReturn(List.of(deleted));

        assertThatThrownBy(() -> service.createListing(createRequest(Set.of(AMENITY_ID))))
                .isInstanceOf(BadRequestException.class);
        verify(listingRepository, never()).save(any());
    }

    // ── updateListing / toggle / delete ──────────────────────────────────────

    @Test
    void updateListingDeniesNonHost() {
        authenticate(OTHER_USER_ID);
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID))
                .thenReturn(Optional.of(ownedListing()));

        assertThatThrownBy(() -> service.updateListing(LISTING_ID, updateRequest(Set.of())))
                .isInstanceOf(AccessDeniedException.class);
        verify(listingMapper, never()).updateListing(any(), any());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void updateListingClearsAmenitiesForEmptySet() {
        Listing listing = ownedListing();
        listing.setAmenities(new java.util.HashSet<>(List.of(activeAmenity())));
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        doAnswer(invocation -> {
            Listing target = invocation.getArgument(1);
            target.setTitle("Updated title");
            return null;
        }).when(listingMapper).updateListing(any(ListingUpdateRequest.class), any(Listing.class));
        saveReturnsArgument();
        when(listingMapper.toDetailedResponse(any(Listing.class)))
                .thenReturn(mock(ListingDetailedResponse.class));

        service.updateListing(LISTING_ID, updateRequest(Set.of()));

        verify(listingRepository).save(listing);
        assertThat(listing.getAmenities()).isEmpty();
        assertThat(listing.getTitle()).isEqualTo("Updated title");
        verify(amenityRepository, never()).findAllById(any());
    }

    @Test
    void updateListingReplacesAmenitiesForNonEmptySet() {
        Listing listing = ownedListing();
        Amenity amenity = activeAmenity();
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        when(amenityRepository.findAllById(Set.of(AMENITY_ID))).thenReturn(List.of(amenity));
        saveReturnsArgument();
        when(listingMapper.toDetailedResponse(any(Listing.class)))
                .thenReturn(mock(ListingDetailedResponse.class));

        service.updateListing(LISTING_ID, updateRequest(Set.of(AMENITY_ID)));

        verify(listingRepository).save(listing);
        assertThat(listing.getAmenities()).containsExactly(amenity);
    }

    @Test
    void deleteListingSoftDeletesAndSaves() {
        Listing listing = ownedListing();
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        saveReturnsArgument();

        service.deleteListing(LISTING_ID);

        verify(listingRepository).save(listing);
        assertThat(listing.getDeletedAt()).isNotNull();
    }

    // ── reads and specifications ─────────────────────────────────────────────

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void searchListingsUsesNullHostIdSpec() {
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setPageNumber(1);
        paginationRequest.setPageSize(6);
        Pageable pageable = PaginationUtil.of(paginationRequest);
        when(listingRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        PageResponse<ListingSummaryResponse> result = service.searchListings(emptyFilter(), paginationRequest);

        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(6);

        ArgumentCaptor<Specification> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(listingRepository).findAll(specCaptor.capture(), eq(pageable));

        Root root = mock(Root.class);
        Path activePath = mock(Path.class);
        Path availablePath = mock(Path.class);
        when(root.get("deletedAt")).thenReturn(mock(Path.class));
        when(root.get("active")).thenReturn(activePath);
        when(root.get("available")).thenReturn(availablePath);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        specCaptor.getValue().toPredicate(root, null, cb);
        verify(cb).isTrue(activePath);
        verify(cb).isTrue(availablePath);
        verify(cb, never()).equal(any(), any());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void getMyListingsUsesCurrentUserIdInSpec() {
        Listing listing = ownedListing();
        ListingSummaryResponse summary = mock(ListingSummaryResponse.class);
        PaginationRequest paginationRequest = new PaginationRequest();
        Pageable pageable = PaginationUtil.of(paginationRequest);
        when(listingRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(listing), pageable, 1));
        when(listingMapper.toSummaryResponse(listing)).thenReturn(summary);

        PageResponse<ListingSummaryResponse> result = service.getMyListings(emptyFilter(), paginationRequest);

        assertThat(result.getContent()).containsExactly(summary);

        ArgumentCaptor<Specification> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(listingRepository).findAll(specCaptor.capture(), eq(pageable));

        Root root = mock(Root.class);
        Path hostPath = mock(Path.class);
        Path hostIdPath = mock(Path.class);
        when(root.get("deletedAt")).thenReturn(mock(Path.class));
        when(root.get("host")).thenReturn(hostPath);
        when(hostPath.get("id")).thenReturn(hostIdPath);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        specCaptor.getValue().toPredicate(root, null, cb);
        verify(cb).equal(hostIdPath, USER_ID);
    }

    @Test
    void getListingByIdFailsWhenMissing() {
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getListingById(LISTING_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getListingByIdMapsExistingListing() {
        Listing listing = ownedListing();
        ListingDetailedResponse expected = mock(ListingDetailedResponse.class);
        when(listingRepository.findByIdAndDeletedAtIsNull(LISTING_ID)).thenReturn(Optional.of(listing));
        when(listingMapper.toDetailedResponse(listing)).thenReturn(expected);

        assertThat(service.getListingById(LISTING_ID)).isSameAs(expected);
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

    private void saveReturnsArgument() {
        when(listingRepository.save(any(Listing.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static ListingCreateRequest createRequest(Set<UUID> amenityIds) {
        return new ListingCreateRequest(
                "Sunny Apartment", "A bright flat near the park", "Great view",
                new BigDecimal("1500.00"), new BigDecimal("100.00"), "Kathmandu",
                27.7, 85.3, PricingType.PER_MONTH, PropertyType.APARTMENT,
                RoomType.ENTIRE_PLACE, 4, 2, 2, 1.5, 900,
                Instant.now().plus(7, ChronoUnit.DAYS), 1, List.of("No smoking"), amenityIds);
    }

    private static ListingUpdateRequest updateRequest(Set<UUID> amenityIds) {
        return new ListingUpdateRequest(
                "Updated title", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, amenityIds);
    }

    private static ListingFilterRequest emptyFilter() {
        return new ListingFilterRequest(null, null, null, null, null, null, null, null, null, null);
    }

    private static Listing ownedListing() {
        Listing listing = new Listing();
        listing.setId(LISTING_ID);
        listing.setHost(user(USER_ID));
        listing.setTitle("Sunny Apartment");
        listing.setRent(new BigDecimal("1500.00"));
        listing.setActive(true);
        listing.setAvailable(true);
        listing.setStatus(ListingStatus.PENDING);
        return listing;
    }

    private static Amenity activeAmenity() {
        Amenity amenity = new Amenity();
        amenity.setId(AMENITY_ID);
        amenity.setName("WiFi");
        amenity.setActive(true);
        return amenity;
    }

    private static User user(UUID id) {
        User user = new User();
        user.setId(id);
        user.setEmail(id + "@example.com");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private static void authenticate(UUID userId) {
        UserPrincipal principal = new UserPrincipal(
                userId, userId + "@example.com", UserRole.USER, UserStatus.ACTIVE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
