package com.anush.hamrospace.user.entity;

import java.time.Instant;
import java.util.UUID;

import com.anush.hamrospace.common.entity.BaseEntity;
import com.anush.hamrospace.user.enums.SubscriptionType;
import com.anush.hamrospace.user.enums.UserRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="users")
public class User extends BaseEntity {
    
    @Column(nullable = false,unique = true)
    private String email;

     @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
     @Column(nullable = false)
    private UserRole userRole;

    @Column(nullable = false)
    private boolean isEmailVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType subscriptionType;

    private Instant emailVerifiedAt;

    private Instant lastLoginAt;

    private Instant termsAcceptedAt;

    private String blockedReason;

    private UUID blockedBy;

    private Instant blockedAt;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private Profile profile;

    // Helper method to keep bi-directional relationship consistent
    public void setProfile(Profile profile) {
        if (profile == null) {
            if (this.profile != null) {
                this.profile.setUser(null);
            }
        } else {
            profile.setUser(this);
        }
        this.profile = profile;
    }

}
