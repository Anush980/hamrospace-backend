package com.anush.hamrospace.auth.entity;

import java.time.Instant;

import com.anush.hamrospace.common.entity.BaseEntity;
import com.anush.hamrospace.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 1000)
    private String token;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress; 

    @Column(name = "user_agent", length = 512)
    private String userAgent; 

    @Column(nullable = false)
    private boolean revoked = false; 

    @Column(nullable = false)
    private boolean rememberMe;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}