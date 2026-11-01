package com.anush.hamrospace.listing.entity;

import java.math.BigDecimal;

import com.anush.hamrospace.common.entity.BaseEntity;
import com.anush.hamrospace.listing.enums.ListingStatus;
import com.anush.hamrospace.listing.enums.PricingType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Listing extends BaseEntity{
    
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal rent;

    private BigDecimal deposit;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PricingType pricingType = PricingType.PER_MONTH;

    @Column(nullable = false)
    private boolean isVerified;
}

