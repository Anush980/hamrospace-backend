package com.anush.hamrospace.listing.repository;

import java.util.Optional;
import java.util.UUID;

import com.anush.hamrospace.common.repository.AbstractRepository;
import com.anush.hamrospace.listing.entity.Listing;

public interface ListingRepository extends AbstractRepository<Listing> {
    

    Optional<Listing> findByIdAndDeletedAtIsNull(UUID id);
    long countByUserIdAndDeletedAtIsNull(UUID userId);
}
