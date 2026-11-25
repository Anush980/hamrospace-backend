package com.anush.hamrospace.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import com.anush.hamrospace.admin.controller.AdminListingController;
import com.anush.hamrospace.admin.controller.AdminReviewController;
import com.anush.hamrospace.admin.controller.AdminUserController;
import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.core.security.config.SecurityConfig;
import com.anush.hamrospace.core.security.constants.PublicPaths;
import com.anush.hamrospace.core.security.user.UserPrincipal;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;

/**
 * Lightweight, framework-free checks of the admin security path. Standalone
 * MockMvc does not execute Spring Security filters or {@code @PreAuthorize};
 * these tests verify the declarations the real security chain relies on.
 */
class AdminSecurityPathTest {

    private static final List<Class<?>> ADMIN_CONTROLLERS = List.of(
            AdminListingController.class, AdminReviewController.class, AdminUserController.class);

    @Test
    void adminControllersAreClassLevelPreAuthorizedForAdminRole() {
        for (Class<?> controller : ADMIN_CONTROLLERS) {
            PreAuthorize annotation = controller.getAnnotation(PreAuthorize.class);
            assertThat(annotation)
                    .as("%s must carry @PreAuthorize", controller.getSimpleName())
                    .isNotNull();
            assertThat(annotation.value()).isEqualTo("hasRole('ADMIN')");
        }
    }

    @Test
    void everyAdminControllerHandlerMethodIsPreAuthorizedForAdminRole() {
        for (Class<?> controller : ADMIN_CONTROLLERS) {
            for (Method method : controller.getDeclaredMethods()) {
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                assertThat(annotation)
                        .as("%s.%s must carry @PreAuthorize", controller.getSimpleName(), method.getName())
                        .isNotNull();
                assertThat(annotation.value())
                        .as("%s.%s role expression", controller.getSimpleName(), method.getName())
                        .isEqualTo("hasRole('ADMIN')");
            }
        }
    }

    @Test
    void adminPathIsNotPublicAndMethodSecurityIsEnabled() {
        String adminBasePath = ApiPath.VERSION + ApiPath.ADMIN + "/**";

        assertThat(adminBasePath).isEqualTo("/v1/admin/**");
        assertThat(Arrays.asList(PublicPaths.PUBLIC_PATHS)).doesNotContain(adminBasePath);
        assertThat(Arrays.stream(PublicPaths.PUBLIC_PATHS)
                .anyMatch(path -> path.startsWith("/v1/admin"))).isFalse();
        assertThat(SecurityConfig.class.isAnnotationPresent(EnableMethodSecurity.class)).isTrue();
    }

    @Test
    void blockedAndSuspendedPrincipalsAreLockedWhileActiveIsUnlocked() {
        assertThat(principal(UserStatus.BLOCKED).isAccountNonLocked()).isFalse();
        assertThat(principal(UserStatus.SUSPENDED).isAccountNonLocked()).isFalse();
        assertThat(principal(UserStatus.ACTIVE).isAccountNonLocked()).isTrue();
    }

    private static UserPrincipal principal(UserStatus status) {
        return new UserPrincipal(
                java.util.UUID.randomUUID(), "user@example.com", UserRole.USER, status);
    }
}
