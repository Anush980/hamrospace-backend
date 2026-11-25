package com.anush.hamrospace.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.anush.hamrospace.admin.dto.request.AdminUserRoleRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserStatusRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserVerificationRequest;
import com.anush.hamrospace.admin.service.AdminUserService;
import com.anush.hamrospace.core.exception.handler.GlobalExceptionHandler;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;

class AdminUserControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private AdminUserService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(AdminUserService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminUserController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void updateRoleBindsRoleAndPassesPathId() throws Exception {
        mockMvc.perform(patch("/v1/admin/users/{id}/role", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<AdminUserRoleRequest> captor = ArgumentCaptor.forClass(AdminUserRoleRequest.class);
        verify(service).updateRole(org.mockito.ArgumentMatchers.eq(USER_ID), captor.capture());
        assertThat(captor.getValue().role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void updateRoleWithNullRoleReturnsValidationFailed() throws Exception {
        mockMvc.perform(patch("/v1/admin/users/{id}/role", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(service, never()).updateRole(any(), any());
    }

    @Test
    void updateStatusBindsStatusAndReason() throws Exception {
        mockMvc.perform(patch("/v1/admin/users/{id}/status", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"BLOCKED\",\"reason\":\"abuse\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<AdminUserStatusRequest> captor = ArgumentCaptor.forClass(AdminUserStatusRequest.class);
        verify(service).updateStatus(org.mockito.ArgumentMatchers.eq(USER_ID), captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(UserStatus.BLOCKED);
        assertThat(captor.getValue().reason()).isEqualTo("abuse");
    }

    @Test
    void updateStatusWithMissingStatusReturnsValidationFailed() throws Exception {
        mockMvc.perform(patch("/v1/admin/users/{id}/status", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verify(service, never()).updateStatus(any(), any());
    }

    @Test
    void updateVerificationBindsFlag() throws Exception {
        mockMvc.perform(patch("/v1/admin/users/{id}/verification", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"verified\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<AdminUserVerificationRequest> captor = ArgumentCaptor.forClass(AdminUserVerificationRequest.class);
        verify(service).updateVerification(org.mockito.ArgumentMatchers.eq(USER_ID), captor.capture());
        assertThat(captor.getValue().verified()).isTrue();
    }

    @Test
    void initiatePasswordResetDelegatesWithPathId() throws Exception {
        mockMvc.perform(post("/v1/admin/users/{id}/password-reset", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).initiatePasswordReset(USER_ID);
    }

    @Test
    void getUserByIdPassesPathVariable() throws Exception {
        mockMvc.perform(get("/v1/admin/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).getUserById(USER_ID);
    }
}
