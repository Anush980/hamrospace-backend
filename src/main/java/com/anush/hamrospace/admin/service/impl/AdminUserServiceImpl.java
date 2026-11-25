package com.anush.hamrospace.admin.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.admin.dto.request.AdminUserFilterRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserRoleRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserStatusRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserVerificationRequest;
import com.anush.hamrospace.admin.dto.response.AdminUserResponse;
import com.anush.hamrospace.admin.service.AdminUserService;
import com.anush.hamrospace.admin.specification.AdminUserSpecification;
import com.anush.hamrospace.auth.repository.RefreshTokenRepository;
import com.anush.hamrospace.auth.service.PasswordResetService;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.ConflictException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;
import com.anush.hamrospace.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetService passwordResetService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getUsers(AdminUserFilterRequest filter, PaginationRequest page) {
        requireAdmin();

        Pageable pageable = PaginationUtil.of(page);
        Page<User> users = userRepository.findAll(AdminUserSpecification.buildSpecification(filter), pageable);
        return PageResponse.from(users, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(UUID id) {
        requireAdmin();
        return toResponse(findUser(id));
    }

    @Override
    public AdminUserResponse updateVerification(UUID id, AdminUserVerificationRequest request) {
        requireAdmin();

        if (request == null || request.verified() == null) {
            throw new ConflictException("Verified flag is required");
        }

        User user = findUser(id);
        user.setEmailVerified(request.verified());
        user.setEmailVerifiedAt(request.verified() ? Instant.now() : null);

        return toResponse(userRepository.save(user));
    }

    @Override
    public AdminUserResponse updateRole(UUID id, AdminUserRoleRequest request) {
        requireAdmin();

        if (request == null || request.role() == null) {
            throw new ConflictException("Role is required");
        }

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User user = findUser(id);

        if (user.getRole() == request.role()) {
            return toResponse(user);
        }

        if (currentUserId.equals(user.getId())) {
            throw new AccessDeniedException("You cannot change your own role");
        }
        if (user.getRole() == UserRole.ADMIN && countAdmins() <= 1) {
            throw new ConflictException("Cannot demote the last admin");
        }

        user.setRole(request.role());
        return toResponse(userRepository.save(user));
    }

    @Override
    public AdminUserResponse updateStatus(UUID id, AdminUserStatusRequest request) {
        requireAdmin();

        if (request == null || request.status() == null) {
            throw new ConflictException("Status is required");
        }

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User user = findUser(id);

        if (user.getStatus() == request.status()) {
            return toResponse(user);
        }

        if (currentUserId.equals(user.getId())) {
            throw new AccessDeniedException("You cannot change your own account status");
        }
        if (user.getRole() == UserRole.ADMIN && request.status() != UserStatus.ACTIVE && countAdmins() <= 1) {
            throw new ConflictException("Cannot suspend or block the last admin");
        }

        Instant now = Instant.now();
        user.setStatus(request.status());
        if (request.status() == UserStatus.ACTIVE) {
            user.setBlockedAt(null);
            user.setBlockedBy(null);
            user.setBlockedReason(null);
        } else {
            user.setBlockedAt(now);
            user.setBlockedBy(currentUserId);
            user.setBlockedReason(request.reason());
            refreshTokenRepository.revokeAllActiveTokensByUser(user, now);
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    public void initiatePasswordReset(UUID id) {
        requireAdmin();
        findUser(id);
        passwordResetService.adminInitiatePasswordReset(id);
    }

    private User findUser(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private long countAdmins() {
        return userRepository.countByRole(UserRole.ADMIN);
    }

    private void requireAdmin() {
        if (SecurityUtil.getCurrentUser().getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Admin access required");
        }
    }

    private AdminUserResponse toResponse(User user) {
        Profile profile = user.getProfile();
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.isEmailVerified(),
                profile != null ? profile.getFullName() : null,
                user.getBlockedReason(),
                user.getCreatedAt());
    }
}
