package com.anush.hamrospace.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.anush.hamrospace.admin.dto.request.AdminUserRoleRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserStatusRequest;
import com.anush.hamrospace.admin.dto.request.AdminUserVerificationRequest;
import com.anush.hamrospace.admin.dto.response.AdminUserResponse;
import com.anush.hamrospace.admin.service.impl.AdminUserServiceImpl;
import com.anush.hamrospace.auth.repository.RefreshTokenRepository;
import com.anush.hamrospace.auth.service.PasswordResetService;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.exception.http.ConflictException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.user.UserPrincipal;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;
import com.anush.hamrospace.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID NON_ADMIN_ID = UUID.randomUUID();

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private AdminUserServiceImpl service;

    @BeforeEach
    void setUp() {
        authenticate(ADMIN_ID, UserRole.ADMIN);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── admin guard ──────────────────────────────────────────────────────────

    @Test
    void nonAdminCannotCallAnyAdminUserMethodAndCannotWrite() {
        authenticate(NON_ADMIN_ID, UserRole.USER);

        assertThatThrownBy(() -> service.getUsers(null, new PaginationRequest()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.getUserById(USER_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.updateVerification(USER_ID, new AdminUserVerificationRequest(true)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.updateRole(USER_ID, new AdminUserRoleRequest(UserRole.ADMIN)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.updateStatus(USER_ID, new AdminUserStatusRequest(UserStatus.BLOCKED, null)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.initiatePasswordReset(USER_ID))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRepository, refreshTokenRepository, passwordResetService);
    }

    // ── reads ────────────────────────────────────────────────────────────────

    @Test
    void getUsersMapsPage() {
        PaginationRequest paginationRequest = new PaginationRequest();
        Pageable pageable = PaginationUtil.of(paginationRequest);
        User user = user(USER_ID, UserRole.USER, UserStatus.ACTIVE);
        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        PageResponse<AdminUserResponse> result = service.getUsers(null, paginationRequest);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(USER_ID);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getUserByIdMapsUserWithProfile() {
        User user = user(USER_ID, UserRole.USER, UserStatus.ACTIVE);
        Profile profile = new Profile();
        profile.setFullName("Anush Shrestha");
        user.setProfile(profile);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));

        AdminUserResponse result = service.getUserById(USER_ID);

        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.role()).isEqualTo(UserRole.USER);
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.fullName()).isEqualTo("Anush Shrestha");
    }

    @Test
    void getUserByIdThrowsWhenMissing() {
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateVerification ───────────────────────────────────────────────────

    @Test
    void updateVerificationTrueSetsTimestampAndSaves() {
        User user = user(USER_ID, UserRole.USER, UserStatus.ACTIVE);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));
        saveReturnsArgument();

        AdminUserResponse result = service.updateVerification(
                USER_ID, new AdminUserVerificationRequest(true));

        assertThat(result.emailVerified()).isTrue();
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void updateVerificationFalseClearsTimestampAndSaves() {
        User user = user(USER_ID, UserRole.USER, UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));
        saveReturnsArgument();

        service.updateVerification(USER_ID, new AdminUserVerificationRequest(false));

        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.getEmailVerifiedAt()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void updateVerificationWithNullFlagIsRejected() {
        assertThatThrownBy(() -> service.updateVerification(USER_ID, new AdminUserVerificationRequest(null)))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(userRepository);
    }

    // ── updateRole ───────────────────────────────────────────────────────────

    @Test
    void updateRolePromotesUserToAdmin() {
        User user = user(USER_ID, UserRole.USER, UserStatus.ACTIVE);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));
        saveReturnsArgument();

        service.updateRole(USER_ID, new AdminUserRoleRequest(UserRole.ADMIN));

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    void updateRoleRejectsChangingOwnRole() {
        User self = user(ADMIN_ID, UserRole.USER, UserStatus.ACTIVE);
        when(userRepository.findByIdAndDeletedAtIsNull(ADMIN_ID)).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> service.updateRole(ADMIN_ID, new AdminUserRoleRequest(UserRole.ADMIN)))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(self.getRole()).isEqualTo(UserRole.USER);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateRoleRejectsDemotingLastAdmin() {
        User lastAdmin = user(USER_ID, UserRole.ADMIN, UserStatus.ACTIVE);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(lastAdmin));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.updateRole(USER_ID, new AdminUserRoleRequest(UserRole.USER)))
                .isInstanceOf(ConflictException.class);
        assertThat(lastAdmin.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateRoleWithNullRoleIsRejected() {
        assertThatThrownBy(() -> service.updateRole(USER_ID, new AdminUserRoleRequest(null)))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(userRepository);
    }

    // ── updateStatus ─────────────────────────────────────────────────────────

    @Test
    void updateStatusRejectsChangingOwnStatus() {
        User self = user(ADMIN_ID, UserRole.ADMIN, UserStatus.ACTIVE);
        when(userRepository.findByIdAndDeletedAtIsNull(ADMIN_ID)).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> service.updateStatus(
                ADMIN_ID, new AdminUserStatusRequest(UserStatus.BLOCKED, "self")))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(self.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void updateStatusRejectsBlockingLastAdmin() {
        User lastAdmin = user(USER_ID, UserRole.ADMIN, UserStatus.ACTIVE);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(lastAdmin));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.updateStatus(
                USER_ID, new AdminUserStatusRequest(UserStatus.BLOCKED, "no reason")))
                .isInstanceOf(ConflictException.class);
        assertThat(lastAdmin.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(refreshTokenRepository);
    }

    @ParameterizedTest
    @EnumSource(value = UserStatus.class, names = { "BLOCKED", "SUSPENDED" })
    void updateStatusToBlockOrSuspendSetsReasonAndRevokesTokens(UserStatus targetStatus) {
        User user = user(USER_ID, UserRole.USER, UserStatus.ACTIVE);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));
        saveReturnsArgument();

        service.updateStatus(USER_ID, new AdminUserStatusRequest(targetStatus, "abuse"));

        assertThat(user.getStatus()).isEqualTo(targetStatus);
        assertThat(user.getBlockedAt()).isNotNull();
        assertThat(user.getBlockedBy()).isEqualTo(ADMIN_ID);
        assertThat(user.getBlockedReason()).isEqualTo("abuse");
        verify(refreshTokenRepository).revokeAllActiveTokensByUser(eq(user), any(Instant.class));
        verify(userRepository).save(user);
    }

    @Test
    void updateStatusToActiveClearsBlockFieldsAndDoesNotRevoke() {
        User user = user(USER_ID, UserRole.USER, UserStatus.BLOCKED);
        user.setBlockedAt(Instant.now().minusSeconds(60));
        user.setBlockedBy(ADMIN_ID);
        user.setBlockedReason("abuse");
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));
        saveReturnsArgument();

        service.updateStatus(USER_ID, new AdminUserStatusRequest(UserStatus.ACTIVE, null));

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getBlockedAt()).isNull();
        assertThat(user.getBlockedBy()).isNull();
        assertThat(user.getBlockedReason()).isNull();
        verify(userRepository).save(user);
        verify(refreshTokenRepository, never()).revokeAllActiveTokensByUser(any(), any());
    }

    @Test
    void updateStatusWithNullStatusIsRejected() {
        assertThatThrownBy(() -> service.updateStatus(USER_ID, new AdminUserStatusRequest(null, null)))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(userRepository);
    }

    // ── initiatePasswordReset ────────────────────────────────────────────────

    @Test
    void initiatePasswordResetDelegatesToPasswordResetService() {
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID))
                .thenReturn(Optional.of(user(USER_ID, UserRole.USER, UserStatus.ACTIVE)));

        service.initiatePasswordReset(USER_ID);

        verify(passwordResetService).adminInitiatePasswordReset(USER_ID);
    }

    @Test
    void initiatePasswordResetForMissingUserDoesNotDelegate() {
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.initiatePasswordReset(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(passwordResetService, never()).adminInitiatePasswordReset(any());
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

    private void saveReturnsArgument() {
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static User user(UUID id, UserRole role, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setEmail(id + "@example.com");
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private static void authenticate(UUID userId, UserRole role) {
        UserPrincipal principal = new UserPrincipal(
                userId, userId + "@example.com", role, UserStatus.ACTIVE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
