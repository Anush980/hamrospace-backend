package com.anush.hamrospace.core.security.util;


import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.anush.hamrospace.core.exception.http.AccessDeniedException;
import com.anush.hamrospace.core.security.user.UserPrincipal;

public final class SecurityUtil {

    private SecurityUtil() {
        throw new UnsupportedOperationException("You are not allowed to instantiate this method");
    }

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        throw new AccessDeniedException("Authentication required to perform this action");
    }

public static String getCurrentUsername(){
    return getCurrentUser().getUsername();
}

public static UUID getCurrentUserId(){
    return getCurrentUser().getId();
}
public static String getCurrentUserEmail(){
    return getCurrentUser().getEmail();
}
}