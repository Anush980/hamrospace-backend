package com.anush.hamrospace.amenity.service;

import java.util.UUID;

import com.anush.hamrospace.amenity.dto.request.AmenityFilterRequest;
import com.anush.hamrospace.amenity.dto.request.AmenityRequest;
import com.anush.hamrospace.amenity.dto.response.AmenityResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;

public interface AmenityService {

    PageResponse<AmenityResponse>  getAllAmenities(AmenityFilterRequest filterRequest,PaginationRequest paginationRequest);

    AmenityResponse createAmenity(AmenityRequest request);

    AmenityResponse updateAmentiy(AmenityRequest request);

    AmenityResponse toggleActiveStatus(UUID id);

    AmenityResponse toggleFeaturedStatus(UUID id);

    void deleteAmenity(UUID id);
}