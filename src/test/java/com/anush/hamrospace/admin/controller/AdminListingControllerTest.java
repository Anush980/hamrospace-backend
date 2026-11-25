package com.anush.hamrospace.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.anush.hamrospace.admin.dto.request.AdminListingActiveRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingStatusRequest;
import com.anush.hamrospace.admin.dto.request.AdminListingVerifiedRequest;
import com.anush.hamrospace.admin.service.AdminListingService;
import com.anush.hamrospace.core.exception.handler.GlobalExceptionHandler;
import com.anush.hamrospace.listing.enums.ListingStatus;

class AdminListingControllerTest {

    private static final UUID LISTING_ID = UUID.randomUUID();

    private AdminListingService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(AdminListingService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminListingController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void updateStatusBindsReasonAndPassesPathId() throws Exception {
        mockMvc.perform(patch("/v1/admin/listings/{id}/status", LISTING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"REJECTED\",\"reason\":\"Fix address\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<AdminListingStatusRequest> captor = ArgumentCaptor.forClass(AdminListingStatusRequest.class);
        verify(service).updateStatus(org.mockito.ArgumentMatchers.eq(LISTING_ID), captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(ListingStatus.REJECTED);
        assertThat(captor.getValue().reason()).isEqualTo("Fix address");
    }

    @Test
    void updateStatusWithoutStatusReturnsValidationFailed() throws Exception {
        mockMvc.perform(patch("/v1/admin/listings/{id}/status", LISTING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Fix address\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(service, never()).updateStatus(any(), any());
    }

    @Test
    void updateVerifiedBindsFlag() throws Exception {
        mockMvc.perform(patch("/v1/admin/listings/{id}/verified", LISTING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"verified\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<AdminListingVerifiedRequest> captor = ArgumentCaptor.forClass(AdminListingVerifiedRequest.class);
        verify(service).updateVerified(org.mockito.ArgumentMatchers.eq(LISTING_ID), captor.capture());
        assertThat(captor.getValue().verified()).isTrue();
    }

    @Test
    void updateVerifiedWithoutFlagReturnsValidationFailed() throws Exception {
        mockMvc.perform(patch("/v1/admin/listings/{id}/verified", LISTING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(service, never()).updateVerified(any(), any());
    }

    @Test
    void updateActiveBindsFlag() throws Exception {
        mockMvc.perform(patch("/v1/admin/listings/{id}/active", LISTING_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<AdminListingActiveRequest> captor = ArgumentCaptor.forClass(AdminListingActiveRequest.class);
        verify(service).updateActive(org.mockito.ArgumentMatchers.eq(LISTING_ID), captor.capture());
        assertThat(captor.getValue().active()).isFalse();
    }

    @Test
    void getListingByIdPassesPathVariable() throws Exception {
        mockMvc.perform(get("/v1/admin/listings/{id}", LISTING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).getListingById(LISTING_ID);
    }
}
