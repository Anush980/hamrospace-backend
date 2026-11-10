package com.anush.hamrospace.review.entity;

import com.anush.hamrospace.common.entity.BaseEntity;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class Review extends BaseEntity {
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name ="author_id",nullable=false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

}
