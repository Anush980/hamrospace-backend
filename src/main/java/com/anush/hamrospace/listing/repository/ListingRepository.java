package com.anush.hamrospace.listing.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.anush.hamrospace.common.repository.AbstractRepository;
import com.anush.hamrospace.listing.entity.Listing;

public interface ListingRepository extends AbstractRepository<Listing> {
    

    Optional<Listing> findByIdAndDeletedAtIsNull(UUID id);

    Page<Listing> findByHostIdAndDeletedAtIsNull(UUID userId,Pageable pageable);
    long countByUserIdAndDeletedAtIsNull(UUID userId);
}
