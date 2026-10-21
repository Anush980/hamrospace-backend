package com.anush.hamrospace.core.security.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;

import lombok.Getter;

@Getter
public class UserPrincipal implements UserDetails {

    private final transient User user;
    private final UUID id;
    private final String email;
    private final UserRole role;
    private final UserStatus status;

    public UserPrincipal(User user) {
        this.user = user;
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.status= user.getStatus();
    }

    public UserPrincipal(UUID id, String email, UserRole role,UserStatus status) {
        this.user = null;
        this.id = id;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        UserRole activeRole = (role != null) ? role : (user != null) ? user.getRole() : UserRole.USER;
        return List.of(new SimpleGrantedAuthority("ROLE_" + activeRole.name()));

    }

    @Override
    public @Nullable String getPassword() {
        return user != null ? user.getPassword() : null;
    }

    @Override
    public String getUsername() {
        return email != null ? email : (user != null ? user.getEmail() : null);
    }

    @Override
    public boolean isAccountNonLocked() {
        return user == null || user.getBlockedAt() == null;
    }

    @Override
    public boolean isEnabled() {
        return status==UserStatus.ACTIVE;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

}
