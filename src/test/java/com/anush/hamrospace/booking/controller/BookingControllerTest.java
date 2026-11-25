package com.anush.hamrospace.booking.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.anush.hamrospace.booking.dto.request.BookingCancelRequest;
import com.anush.hamrospace.booking.dto.request.BookingCreateRequest;
import com.anush.hamrospace.booking.dto.request.BookingFilterRequest;
import com.anush.hamrospace.booking.dto.response.BookingDetailedResponse;
import com.anush.hamrospace.booking.dto.response.BookingSummaryResponse;
import com.anush.hamrospace.booking.enums.BookingFeeStatus;
import com.anush.hamrospace.booking.enums.BookingStatus;
import com.anush.hamrospace.booking.service.BookingService;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.core.exception.handler.GlobalExceptionHandler;

class BookingControllerTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();
    private static final UUID LISTING_ID = UUID.randomUUID();

    private BookingService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(BookingService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new BookingController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createBookingWithValidBodyReturnsOkAndDelegates() throws Exception {
        Instant moveInDate = Instant.now().plus(30, ChronoUnit.DAYS);
        when(service.createBooking(any(BookingCreateRequest.class))).thenReturn(detailedResponse());
        String body = """
                {
                  "listingId": "%s",
                  "moveInDate": "%s",
                  "numberOfGuests": 2,
                  "notes": "Please call first"
                }
                """.formatted(LISTING_ID, moveInDate);

        mockMvc.perform(post("/v1/bookings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(BOOKING_ID.toString()));

        ArgumentCaptor<BookingCreateRequest> captor = ArgumentCaptor.forClass(BookingCreateRequest.class);
        verify(service).createBooking(captor.capture());
        assertThat(captor.getValue().listingId()).isEqualTo(LISTING_ID);
        assertThat(captor.getValue().moveInDate()).isEqualTo(moveInDate);
        assertThat(captor.getValue().numberOfGuests()).isEqualTo(2);
        assertThat(captor.getValue().notes()).isEqualTo("Please call first");
    }

    @Test
    void createBookingRejectsMissingListingId() throws Exception {
        String body = """
                {"moveInDate": "%s", "numberOfGuests": 1}
                """.formatted(Instant.now().plus(30, ChronoUnit.DAYS));

        mockMvc.perform(post("/v1/bookings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(service, never()).createBooking(any());
    }

    @Test
    void createBookingRejectsPastMoveInDate() throws Exception {
        String body = """
                {"listingId": "%s", "moveInDate": "%s", "numberOfGuests": 1}
                """.formatted(LISTING_ID, Instant.now().minus(1, ChronoUnit.DAYS));

        mockMvc.perform(post("/v1/bookings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(service, never()).createBooking(any());
    }

    @Test
    void cancelBookingPassesPathIdAndCancellationReason() throws Exception {
        when(service.cancelBooking(any(UUID.class), any(BookingCancelRequest.class)))
                .thenReturn(detailedResponse());

        mockMvc.perform(patch("/v1/bookings/{id}", BOOKING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cancellationReason\": \"Plans changed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<BookingCancelRequest> captor = ArgumentCaptor.forClass(BookingCancelRequest.class);
        verify(service).cancelBooking(org.mockito.ArgumentMatchers.eq(BOOKING_ID), captor.capture());
        assertThat(captor.getValue().cancellationReason()).isEqualTo("Plans changed");
    }

    @Test
    void cancelBookingRejectsBlankReason() throws Exception {
        mockMvc.perform(patch("/v1/bookings/{id}", BOOKING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cancellationReason\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(service, never()).cancelBooking(any(), any());
    }

    @Test
    void confirmBookingDelegates() throws Exception {
        when(service.confirmBooking(BOOKING_ID)).thenReturn(detailedResponse());

        mockMvc.perform(patch("/v1/bookings/{id}/confirm", BOOKING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).confirmBooking(BOOKING_ID);
    }

    @Test
    void confirmCheckInDelegates() throws Exception {
        when(service.confirmCheckIn(BOOKING_ID)).thenReturn(detailedResponse());

        mockMvc.perform(patch("/v1/bookings/{id}/check-in", BOOKING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).confirmCheckIn(BOOKING_ID);
    }

    @Test
    void markNoShowDelegates() throws Exception {
        when(service.markNoShow(BOOKING_ID)).thenReturn(detailedResponse());

        mockMvc.perform(patch("/v1/bookings/{id}/no-show", BOOKING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).markNoShow(BOOKING_ID);
    }

    @Test
    void getBookingByIdDelegates() throws Exception {
        when(service.getBookingById(BOOKING_ID)).thenReturn(detailedResponse());

        mockMvc.perform(get("/v1/bookings/{id}", BOOKING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(BOOKING_ID.toString()));

        verify(service).getBookingById(BOOKING_ID);
    }

    @Test
    void getMyBookingsDelegatesFiltersAndPagination() throws Exception {
        when(service.getMyBookings(any(BookingFilterRequest.class), any(PaginationRequest.class)))
                .thenReturn(summaryPage());

        mockMvc.perform(get("/v1/bookings/me")
                .param("pageNumber", "1")
                .param("pageSize", "7")
                .param("sortBy", "moveInDate")
                .param("sortDirection", "asc")
                .param("bookingStatus", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<BookingFilterRequest> filterCaptor = ArgumentCaptor.forClass(BookingFilterRequest.class);
        ArgumentCaptor<PaginationRequest> pageCaptor = ArgumentCaptor.forClass(PaginationRequest.class);
        verify(service).getMyBookings(filterCaptor.capture(), pageCaptor.capture());
        assertThat(filterCaptor.getValue().bookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(pageCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getPageSize()).isEqualTo(7);
        assertThat(pageCaptor.getValue().getSortBy()).isEqualTo("moveInDate");
        assertThat(pageCaptor.getValue().getSortDirection()).isEqualTo("asc");
    }

    private static BookingDetailedResponse detailedResponse() {
        return new BookingDetailedResponse(
                BOOKING_ID, LISTING_ID, "Sunny apartment", "Kathmandu",
                new BigDecimal("1000.00"), new BigDecimal("50.00"),
                UUID.randomUUID(), "Guest Name", "guest@example.com", 2,
                Instant.now().plus(30, ChronoUnit.DAYS),
                BookingStatus.PENDING, BookingFeeStatus.PENDING,
                BigDecimal.ZERO, null, null, null, null, null, null);
    }

    private static PageResponse<BookingSummaryResponse> summaryPage() {
        return PageResponse.<BookingSummaryResponse>builder()
                .content(List.of())
                .page(1)
                .size(7)
                .totalElements(0)
                .totalPages(0)
                .build();
    }
}
