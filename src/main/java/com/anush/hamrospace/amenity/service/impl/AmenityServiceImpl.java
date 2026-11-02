package com.anush.hamrospace.amenity.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.amenity.dto.request.AmenityFilterRequest;
import com.anush.hamrospace.amenity.dto.request.AmenityRequest;
import com.anush.hamrospace.amenity.dto.response.AmenityResponse;
import com.anush.hamrospace.amenity.service.AmenityService;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AmenityServiceImpl implements AmenityService {@Override
    public PageResponse<AmenityResponse> getAllAmenities(AmenityFilterRequest filterRequest,
            PaginationRequest paginationRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllAmenities'");
    }

    @Override
    public AmenityResponse createAmenity(AmenityRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAmenity'");
    }

    @Override
    public AmenityResponse updateAmentiy(AmenityRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAmentiy'");
    }

    @Override
    public AmenityResponse toggleActiveStatus(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toggleActiveStatus'");
    }

    @Override
    public AmenityResponse toggleFeaturedStatus(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toggleFeaturedStatus'");
    }

    @Override
    public void deleteAmenity(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAmenity'");
    }
    
}
