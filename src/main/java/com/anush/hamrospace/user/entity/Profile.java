package com.anush.hamrospace.user.entity;

import com.anush.hamrospace.common.entity.BaseEntity;
import com.anush.hamrospace.user.enums.AddressType;
import com.anush.hamrospace.user.enums.PreferredLanguage;
import com.anush.hamrospace.user.enums.UiTheme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "profiles")
public class Profile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    private String fullName;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    private String address;

    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    private PreferredLanguage preferredLanguage=PreferredLanguage.ENGLISH;

    @Enumerated(EnumType.STRING)
    private UiTheme uiTheme=UiTheme.SYSTEM;


}
