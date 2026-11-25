package com.anush.hamrospace.admin.service;

import java.util.UUID;

import com.anush.hamrospace.admin.dto.request.AdminUserFilterRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserRoleRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserStatusRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserVerificationRequest;
import com.anush.hamrospace.admin.dto.response.AdminUserResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;

public interface AdminUserService {

    PageResponse<AdminUserResponse> getUsers(AdminUserFilterRequest filter, PaginationRequest page);

    AdminUserResponse getUserById(UUID id);

    AdminUserResponse updateVerification(UUID id, AdminUserVerificationRequest request);

    AdminUserResponse updateRole(UUID id, AdminUserRoleRequest request);

    AdminUserResponse updateStatus(UUID id, AdminUserStatusRequest request);

    void initiatePasswordReset(UUID id);
}
