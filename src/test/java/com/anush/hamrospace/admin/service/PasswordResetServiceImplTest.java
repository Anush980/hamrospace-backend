package com.anush.hamrospace.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.anush.hamrospace.auth.dto.request.ResetPasswordRequest;
import com.anush.hamrospace.auth.entity.PasswordResetToken;
import com.anush.hamrospace.auth.event.ForgotPasswordEvent;
import com.anush.hamrospace.auth.event.PasswordChangedEvent;
import com.anush.hamrospace.auth.repository.PasswordResetTokenRepository;
import com.anush.hamrospace.auth.repository.RefreshTokenRepository;
import com.anush.hamrospace.auth.service.TokenLifeCycleService;
import com.anush.hamrospace.auth.service.impl.PasswordResetServiceImpl;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.ClientMetaDataUtils;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;
import com.anush.hamrospace.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    private static final String EMAIL = "guest@example.com";
    private static final String RAW_OTP = "123456";
    private static final String NEW_PASSWORD = "newPassword123";
    private static final String OLD_PASSWORD_HASH = "old-hash";
    private static final String ENCODED_PASSWORD = "encoded-new-password";

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private TokenLifeCycleService tokenLifeCycleService;
    @Mock
    private SecureRandom secureRandom;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ClientMetaDataUtils clientMetaDataUtils;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PasswordResetServiceImpl service;

    // ── adminInitiatePasswordReset ───────────────────────────────────────────

    @Test
    void adminInitiatePasswordResetForMissingUserThrowsNotFoundAndDoesNotPublish() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adminInitiatePasswordReset(userId))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(passwordResetTokenRepository, eventPublisher);
    }

    @Test
    void adminInitiatePasswordResetInvalidatesOldTokensSavesTokenForTargetAndPublishesOneEvent() {
        UUID userId = UUID.randomUUID();
        User target = user(userId, EMAIL);
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(target));
        when(secureRandom.nextInt(1_000_000)).thenReturn(123456);
        Instant before = Instant.now();

        service.adminInitiatePasswordReset(userId);

        verify(passwordResetTokenRepository).invalidateAllByUser(eq(target), any(Instant.class));

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository, times(1)).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isSameAs(target);
        assertThat(savedToken.getToken()).isEqualTo(hash(RAW_OTP));
        assertThat(savedToken.getExpiryDate())
                .isAfter(before.plus(14, ChronoUnit.MINUTES))
                .isBefore(Instant.now().plus(16, ChronoUnit.MINUTES));

        ArgumentCaptor<ForgotPasswordEvent> eventCaptor = ArgumentCaptor.forClass(ForgotPasswordEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        verify(eventPublisher, times(1)).publishEvent(any(ForgotPasswordEvent.class));
        ForgotPasswordEvent event = eventCaptor.getValue();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.email()).isEqualTo(EMAIL);
        assertThat(event.fullName()).isEqualTo("Guest User");
        assertThat(event.expiresAt()).isEqualTo(savedToken.getExpiryDate());
    }

    // ── resetPassword ────────────────────────────────────────────────────────

    @Test
    void resetPasswordRejectsTokenOwnedByAnotherUserWithoutChangingPassword() {
        User target = user(UUID.randomUUID(), EMAIL);
        target.setPassword(OLD_PASSWORD_HASH);
        PasswordResetToken token = validToken(user(UUID.randomUUID(), "other@example.com"));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(target));
        when(passwordResetTokenRepository.findByToken(hash(RAW_OTP))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(request(RAW_OTP)))
                .isInstanceOf(BadRequestException.class);

        assertThat(target.getPassword()).isEqualTo(OLD_PASSWORD_HASH);
        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeAllActiveTokensByUser(any(), any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void resetPasswordWithUnknownTokenIsRejected() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(UUID.randomUUID(), EMAIL)));
        when(passwordResetTokenRepository.findByToken(hash(RAW_OTP))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(request(RAW_OTP)))
                .isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPasswordWithUsedTokenIsRejected() {
        User target = user(UUID.randomUUID(), EMAIL);
        PasswordResetToken token = validToken(target);
        token.setUsedAt(Instant.now().minusSeconds(10));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(target));
        when(passwordResetTokenRepository.findByToken(hash(RAW_OTP))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(request(RAW_OTP)))
                .isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPasswordHappyPathEncodesSavesRevokesAndPublishesEvent() {
        UUID userId = UUID.randomUUID();
        User target = user(userId, EMAIL);
        target.setPassword(OLD_PASSWORD_HASH);
        PasswordResetToken token = validToken(target);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(target));
        when(passwordResetTokenRepository.findByToken(hash(RAW_OTP))).thenReturn(Optional.of(token));
        when(passwordEncoder.matches(NEW_PASSWORD, OLD_PASSWORD_HASH)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(clientMetaDataUtils.extractDeviceInfo(httpServletRequest)).thenReturn("Linux Desktop");
        when(clientMetaDataUtils.extractIpAddress(httpServletRequest)).thenReturn("127.0.0.1");
        when(clientMetaDataUtils.extractUserAgent(httpServletRequest)).thenReturn("JUnit");

        service.resetPassword(request(RAW_OTP));

        assertThat(target.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(token.getUsedAt()).isNotNull();
        verify(passwordResetTokenRepository).save(token);
        verify(userRepository).save(target);
        verify(refreshTokenRepository).revokeAllActiveTokensByUser(eq(target), any(Instant.class));

        ArgumentCaptor<PasswordChangedEvent> eventCaptor = ArgumentCaptor.forClass(PasswordChangedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        PasswordChangedEvent event = eventCaptor.getValue();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.email()).isEqualTo(EMAIL);
        assertThat(event.fullName()).isEqualTo("Guest User");
        assertThat(event.deviceInfo()).isEqualTo("Linux Desktop");
        assertThat(event.ipAddress()).isEqualTo("127.0.0.1");
        assertThat(event.userAgent()).isEqualTo("JUnit");
    }

    @Test
    void resetPasswordRejectsReusingCurrentPassword() {
        User target = user(UUID.randomUUID(), EMAIL);
        target.setPassword(OLD_PASSWORD_HASH);
        PasswordResetToken token = validToken(target);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(target));
        when(passwordResetTokenRepository.findByToken(hash(RAW_OTP))).thenReturn(Optional.of(token));
        when(passwordEncoder.matches(NEW_PASSWORD, OLD_PASSWORD_HASH)).thenReturn(true);

        assertThatThrownBy(() -> service.resetPassword(request(RAW_OTP)))
                .isInstanceOf(BadRequestException.class);
        assertThat(target.getPassword()).isEqualTo(OLD_PASSWORD_HASH);
        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeAllActiveTokensByUser(any(), any());
    }

    // ── fixtures ─────────────────────────────────────────────────────────────

    private static ResetPasswordRequest request(String token) {
        return new ResetPasswordRequest(EMAIL, token, NEW_PASSWORD);
    }

    private static PasswordResetToken validToken(User owner) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(hash(RAW_OTP));
        token.setUser(owner);
        token.setExpiryDate(Instant.now().plus(15, ChronoUnit.MINUTES));
        return token;
    }

    private static User user(UUID id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        Profile profile = new Profile();
        profile.setFullName("Guest User");
        user.setProfile(profile);
        return user;
    }

    /** Same hashing algorithm as production: SHA-256, Base64 URL, no padding. */
    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
