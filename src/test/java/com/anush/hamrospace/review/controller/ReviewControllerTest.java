package com.anush.hamrospace.review.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.core.exception.handler.GlobalExceptionHandler;
import com.anush.hamrospace.review.dto.request.ReviewCreateRequest;
import com.anush.hamrospace.review.service.ReviewService;

class ReviewControllerTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();
    private static final UUID LISTING_ID = UUID.randomUUID();

    private ReviewService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(ReviewService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ReviewController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createReviewWithValidBodyReturnsOkAndDelegates() throws Exception {
        String body = """
                {
                  "bookingId": "%s",
                  "listingId": "%s",
                  "rating": 5,
                  "comment": "Great place"
                }
                """.formatted(BOOKING_ID, LISTING_ID);

        mockMvc.perform(post("/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<ReviewCreateRequest> captor = ArgumentCaptor.forClass(ReviewCreateRequest.class);
        verify(service).createReview(captor.capture());
        assertThat(captor.getValue().bookingId()).isEqualTo(BOOKING_ID);
        assertThat(captor.getValue().listingId()).isEqualTo(LISTING_ID);
        assertThat(captor.getValue().rating()).isEqualTo(5);
        assertThat(captor.getValue().comment()).isEqualTo("Great place");
    }

    @Test
    void createReviewWithInvalidRatingReturnsValidationFailed() throws Exception {
        String body = """
                {
                  "bookingId": "%s",
                  "listingId": "%s",
                  "rating": 6,
                  "comment": "Great place"
                }
                """.formatted(BOOKING_ID, LISTING_ID);

        mockMvc.perform(post("/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(service, never()).createReview(any());
    }

    @Test
    void createReviewWithMissingBodyFieldsReturnsValidationFailed() throws Exception {
        mockMvc.perform(post("/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"Great place\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(service, never()).createReview(any());
    }

    @Test
    void getListingReviewsPassesPathVariableAndPagination() throws Exception {
        mockMvc.perform(get("/v1/reviews/listing/{listingId}", LISTING_ID)
                .param("pageSize", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).getListingReviews(eq(LISTING_ID), any(PaginationRequest.class));
    }
}
