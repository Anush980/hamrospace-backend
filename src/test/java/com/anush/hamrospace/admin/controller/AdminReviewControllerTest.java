package com.anush.hamrospace.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.anush.hamrospace.admin.dto.request.AdminReviewFilterRequest;
import com.anush.hamrospace.admin.service.AdminReviewService;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.core.exception.handler.GlobalExceptionHandler;

class AdminReviewControllerTest {

    private static final UUID REVIEW_ID = UUID.randomUUID();

    private AdminReviewService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(AdminReviewService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminReviewController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void hideDelegatesWithPathId() throws Exception {
        mockMvc.perform(patch("/v1/admin/reviews/{id}/hide", REVIEW_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).hide(REVIEW_ID);
    }

    @Test
    void unhideDelegatesWithPathId() throws Exception {
        mockMvc.perform(patch("/v1/admin/reviews/{id}/unhide", REVIEW_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).unhide(REVIEW_ID);
    }

    @Test
    void removeDelegatesWithPathId() throws Exception {
        mockMvc.perform(delete("/v1/admin/reviews/{id}", REVIEW_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).remove(REVIEW_ID);
    }

    @Test
    void getReviewsDelegatesWithFilterAndPagination() throws Exception {
        mockMvc.perform(get("/v1/admin/reviews")
                .param("pageSize", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).getReviews(any(AdminReviewFilterRequest.class), any(PaginationRequest.class));
    }
}
