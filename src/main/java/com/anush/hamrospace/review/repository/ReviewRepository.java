package com.anush.hamrospace.review.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anush.hamrospace.common.repository.AbstractRepository;
import com.anush.hamrospace.review.entity.Review;

public interface ReviewRepository extends AbstractRepository<Review> {

    boolean existsByBookingId(UUID bookingId);

    List<Review> findByListingIdAndHiddenFalseAndDeletedAtIsNull(UUID listingId);

    @Query("select count(r) from Review r where r.listing.id = :listingId and r.hidden = false and r.deletedAt is null")
    long countVisibleByListingId(@Param("listingId") UUID listingId);

    @Query("select avg(r.rating) from Review r where r.listing.id = :listingId and r.hidden = false and r.deletedAt is null")
    BigDecimal averageVisibleByListingId(@Param("listingId") UUID listingId);
}
