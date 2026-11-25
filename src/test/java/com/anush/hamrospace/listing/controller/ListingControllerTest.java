package com.anush.hamrospace.listing.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.core.exception.handler.GlobalExceptionHandler;
import com.anush.hamrospace.listing.dto.request.ListingCreateRequest;
import com.anush.hamrospace.listing.dto.request.ListingFilterRequest;
import com.anush.hamrospace.listing.dto.request.ListingUpdateRequest;
import com.anush.hamrospace.listing.dto.response.ListingSummaryResponse;
import com.anush.hamrospace.listing.enums.PricingType;
import com.anush.hamrospace.listing.enums.PropertyType;
import com.anush.hamrospace.listing.enums.RoomType;
import com.anush.hamrospace.listing.service.ListingService;

class ListingControllerTest {

    private static final UUID LISTING_ID = UUID.randomUUID();

    private ListingService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(ListingService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ListingController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createListingWithValidBodyReturnsOkAndDelegates() throws Exception {
        String body = """
                {
                  "title": "Sunny Apartment",
                  "description": "A bright flat near the park",
                  "rent": 1500.00,
                  "address": "Kathmandu",
                  "latitude": 27.7,
                  "longitude": 85.3,
                  "pricingType": "PER_MONTH",
                  "propertyType": "APARTMENT",
                  "roomType": "ENTIRE_PLACE",
                  "maxGuests": 4
                }
                """;

        mockMvc.perform(post("/v1/listings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<ListingCreateRequest> captor = ArgumentCaptor.forClass(ListingCreateRequest.class);
        verify(service).createListing(captor.capture());
        assertThat(captor.getValue().title()).isEqualTo("Sunny Apartment");
        assertThat(captor.getValue().rent()).isEqualByComparingTo("1500.00");
        assertThat(captor.getValue().pricingType()).isEqualTo(PricingType.PER_MONTH);
        assertThat(captor.getValue().propertyType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(captor.getValue().roomType()).isEqualTo(RoomType.ENTIRE_PLACE);
        assertThat(captor.getValue().maxGuests()).isEqualTo(4);
    }

    @Test
    void createListingRejectsInvalidBody() throws Exception {
        String body = """
                {
                  "title": "",
                  "description": "",
                  "rent": 0,
                  "latitude": 27.7,
                  "longitude": 85.3,
                  "pricingType": "PER_MONTH",
                  "propertyType": "APARTMENT",
                  "roomType": "ENTIRE_PLACE"
                }
                """;

        mockMvc.perform(post("/v1/listings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(service, never()).createListing(any());
    }

    @Test
    void updateListingPassesPathIdAndBody() throws Exception {
        mockMvc.perform(patch("/v1/listings/{id}", LISTING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"New title\",\"amenityIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<ListingUpdateRequest> requestCaptor = ArgumentCaptor.forClass(ListingUpdateRequest.class);
        verify(service).updateListing(org.mockito.ArgumentMatchers.eq(LISTING_ID), requestCaptor.capture());
        assertThat(requestCaptor.getValue().title()).isEqualTo("New title");
        assertThat(requestCaptor.getValue().amenityIds()).isEmpty();
    }

    @Test
    void toggleActiveStatusDelegates() throws Exception {
        when(service.toggleActiveStatus(LISTING_ID)).thenReturn(summary());

        mockMvc.perform(patch("/v1/listings/{id}/toggle-active", LISTING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(LISTING_ID.toString()));

        verify(service).toggleActiveStatus(LISTING_ID);
    }

    @Test
    void deleteListingDelegates() throws Exception {
        mockMvc.perform(delete("/v1/listings/{id}", LISTING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).deleteListing(LISTING_ID);
    }

    @Test
    void publicSearchDelegatesWithFiltersAndPagination() throws Exception {
        when(service.searchListings(any(ListingFilterRequest.class), any(PaginationRequest.class)))
                .thenReturn(emptyPage());

        mockMvc.perform(get("/v1/listings")
                .param("keyword", "flat")
                .param("pageSize", "7")
                .param("sortBy", "rent")
                .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<ListingFilterRequest> filterCaptor = ArgumentCaptor.forClass(ListingFilterRequest.class);
        ArgumentCaptor<PaginationRequest> pageCaptor = ArgumentCaptor.forClass(PaginationRequest.class);
        verify(service).searchListings(filterCaptor.capture(), pageCaptor.capture());
        assertThat(filterCaptor.getValue().keyword()).isEqualTo("flat");
        assertThat(pageCaptor.getValue().getPageSize()).isEqualTo(7);
        assertThat(pageCaptor.getValue().getSortBy()).isEqualTo("rent");
        assertThat(pageCaptor.getValue().getSortDirection()).isEqualTo("asc");
    }

    @Test
    void getListingByIdDelegates() throws Exception {
        mockMvc.perform(get("/v1/listings/{id}", LISTING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).getListingById(LISTING_ID);
    }

    @Test
    void getMyListingsDelegatesWithPagination() throws Exception {
        when(service.getMyListings(any(ListingFilterRequest.class), any(PaginationRequest.class)))
                .thenReturn(emptyPage());

        mockMvc.perform(get("/v1/listings/me")
                .param("pageNumber", "2")
                .param("pageSize", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<PaginationRequest> pageCaptor = ArgumentCaptor.forClass(PaginationRequest.class);
        verify(service).getMyListings(any(ListingFilterRequest.class), pageCaptor.capture());
        assertThat(pageCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageCaptor.getValue().getPageSize()).isEqualTo(9);
    }

    private static ListingSummaryResponse summary() {
        return new ListingSummaryResponse(
                LISTING_ID, "Sunny Apartment", "sunny-apartment-abcd1234", "Great view",
                new BigDecimal("1500.00"), PricingType.PER_MONTH, "Kathmandu",
                PropertyType.APARTMENT, RoomType.ENTIRE_PLACE, false, true,
                BigDecimal.ZERO, 0, null, null);
    }

    private static PageResponse<ListingSummaryResponse> emptyPage() {
        return PageResponse.<ListingSummaryResponse>builder()
                .content(List.of())
                .page(0)
                .size(10)
                .totalElements(0)
                .totalPages(0)
                .build();
    }
}
