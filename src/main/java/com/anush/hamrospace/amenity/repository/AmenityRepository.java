package com.anush.hamrospace.amenity.repository;

import com.anush.hamrospace.amenity.entity.Amenity;
import com.anush.hamrospace.amenity.enums.AmenityCategory;
import com.anush.hamrospace.common.repository.AbstractRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AmenityRepository extends AbstractRepository<Amenity> {

    List<Amenity> findByActiveTrue();

    Optional<Amenity> findByIdAndDeletedAtIsNull(UUID id);

    List<Amenity> findByFeaturedTrueAndActiveTrue();

    List<Amenity> findByCategoryAndActiveTrue(AmenityCategory category);

    boolean existsByName(String name);

}