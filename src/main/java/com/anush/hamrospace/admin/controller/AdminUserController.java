package com.anush.hamrospace.admin.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.admin.dto.request.AdminUserFilterRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserRoleRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserStatusRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserVerificationRequest;
import com.anush.hamrospace.admin.dto.response.AdminUserResponse;
import com.anush.hamrospace.admin.service.AdminUserService;
import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION+ApiPath.ADMIN+ApiPath.USERS)
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users for administration")
    public ApiResponse<PageResponse<AdminUserResponse>> getUsers(
            @Valid AdminUserFilterRequest filter,
            @Valid PaginationRequest page) {
        return ApiResponse.ok("Users fetched successfully", adminUserService.getUsers(filter, page));
    }

    @GetMapping(ApiPath.ID)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a user for administration")
    public ApiResponse<AdminUserResponse> getUserById(@PathVariable("id") UUID id) {
        return ApiResponse.ok("User fetched successfully", adminUserService.getUserById(id));
    }

    @PatchMapping(ApiPath.ID + "/verification")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set a user's email verification status")
    public ApiResponse<AdminUserResponse> updateVerification(@PathVariable("id") UUID id,
            @Valid @RequestBody AdminUserVerificationRequest request) {
        return ApiResponse.ok("Verification status updated successfully",
                adminUserService.updateVerification(id, request));
    }

    @PatchMapping(ApiPath.ID + "/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change a user's role")
    public ApiResponse<AdminUserResponse> updateRole(@PathVariable("id") UUID id,
            @Valid @RequestBody AdminUserRoleRequest request) {
        return ApiResponse.ok("Role updated successfully", adminUserService.updateRole(id, request));
    }

    @PatchMapping(ApiPath.ID + "/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change a user's account status")
    public ApiResponse<AdminUserResponse> updateStatus(@PathVariable("id") UUID id,
            @Valid @RequestBody AdminUserStatusRequest request) {
        return ApiResponse.ok("Account status updated successfully", adminUserService.updateStatus(id, request));
    }

    @PostMapping(ApiPath.ID + ApiPath.RESET_PASSWORD)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send a password reset email to a user")
    public ApiResponse<Void> initiatePasswordReset(@PathVariable("id") UUID id) {
        adminUserService.initiatePasswordReset(id);
        return ApiResponse.ok("Password reset email sent successfully");
    }
}
