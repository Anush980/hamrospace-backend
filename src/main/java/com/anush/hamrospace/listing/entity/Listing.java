package com.anush.hamrospace.listing.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Point;

import com.anush.hamrospace.amenity.entity.Amenity;
import com.anush.hamrospace.common.entity.BaseEntity;
import com.anush.hamrospace.listing.enums.ListingStatus;
import com.anush.hamrospace.listing.enums.PricingType;
import com.anush.hamrospace.listing.enums.PropertyType;
import com.anush.hamrospace.listing.enums.RoomType;
import com.anush.hamrospace.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Listing extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "tagline", length = 150)
    private String tagline;

    @ElementCollection
    @CollectionTable(name = "listing_house_rules", joinColumns = @JoinColumn(name = "listing_id"))
    @Column(name = "rule_text")
    private List<String> houseRules = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ListingImage> images = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal rent;

    private BigDecimal deposit;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point coordinates;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PricingType pricingType = PricingType.PER_MONTH;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Column(name = "max_guests", nullable = false)
    private Integer maxGuests = 1;

    @Column(nullable = false)
    private Integer bedrooms = 1;

    @Column(nullable = false)
    private Integer beds = 1;

    @Column(nullable = false)
    private Double bathrooms = 1.0;

    @Column(name = "area_sqft")
    private Integer areaSqFt;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    @Column(name = "available_from")
    private Instant availableFrom;

    @Column(name = "min_stay_months")
    private Integer minStayMonths = 1;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "is_active", nullable = false)
    private boolean active= true;

    @Column(name = "average_rating", precision = 3, scale = 2, nullable = false)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @ManyToMany
    @JoinTable(name = "listing_amenities", joinColumns = @JoinColumn(name = "listing_id"), inverseJoinColumns = @JoinColumn(name = "amenity_id"))
    private Set<Amenity> amenities = new HashSet<>();

    // helper
    public void addImage(ListingImage image) {
        images.add(image);
        image.setListing(this);
    }

    public void removeImage(ListingImage image) {
        images.remove(image);
        image.setListing(null);
    }

public String getCoverImageUrl() {
    return images.stream()
            .filter(img -> img != null && img.isPrimary())
            .map(img -> img != null ? img.getImageUrl() : null) // Explicit null safety check
            .findFirst()
            .orElseGet(() -> images.isEmpty() ? null : images.get(0).getImageUrl());
}

public Double getLatitude() {
        return coordinates != null ? coordinates.getY() : null;
    }

    public Double getLongitude() {
        return coordinates != null ? coordinates.getX() : null;
    }
}
