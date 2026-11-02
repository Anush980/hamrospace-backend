package com.anush.hamrospace.amenity.repository;

import com.anush.hamrospace.amenity.entity.Amenity;
import com.anush.hamrospace.amenity.enums.AmenityCategory;
import com.anush.hamrospace.common.repository.AbstractRepository;
import java.util.List;

public interface AmenityRepository extends AbstractRepository<Amenity> {

    List<Amenity> findByActiveTrue();

    List<Amenity> findByFeaturedTrueAndActiveTrue();

    List<Amenity> findByCategoryAndActiveTrue(AmenityCategory category);

    boolean existsByName(String name);
}