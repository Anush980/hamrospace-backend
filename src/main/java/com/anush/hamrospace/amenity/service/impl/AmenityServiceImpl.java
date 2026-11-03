package com.anush.hamrospace.amenity.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.amenity.dto.request.AmenityFilterRequest;
import com.anush.hamrospace.amenity.dto.request.AmenityRequest;
import com.anush.hamrospace.amenity.dto.response.AmenityResponse;
import com.anush.hamrospace.amenity.entity.Amenity;
import com.anush.hamrospace.amenity.mapper.AmenityMapper;
import com.anush.hamrospace.amenity.repository.AmenityRepository;
import com.anush.hamrospace.amenity.service.AmenityService;
import com.anush.hamrospace.amenity.specification.AmenitySpecification;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;
import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;

    @Override
    public PageResponse<AmenityResponse> getAllAmenities(AmenityFilterRequest filterRequest,
            PaginationRequest paginationRequest) {

        Pageable pageable = PaginationUtil.of(paginationRequest);

        Specification<Amenity> spec = AmenitySpecification.buildSpecification(filterRequest);
        Page<Amenity> amenityPage = amenityRepository.findAll(spec, pageable);
        Page<AmenityResponse> responsePage = amenityPage.map(amenityMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    public AmenityResponse createAmenity(AmenityRequest request) {

        if (amenityRepository.existsByName(request.name())) {
            throw new BadRequestException("Amenity with this name already exists");
        }

        Amenity amenity = amenityMapper.toEntity(request);
        Amenity savedAmenity = amenityRepository.save(amenity);
        return amenityMapper.toResponse(savedAmenity);
    }

    @Override
    public AmenityResponse updateAmenity(UUID id, AmenityRequest request) {

        Amenity existingAmenity = amenityRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BadRequestException("Amenity not found"));

        if (amenityRepository.existsByName(request.name())) {
            throw new BadRequestException("Amenity with this name already exists");
        }

        amenityMapper.updateEntityFromDto(request, existingAmenity);
        Amenity updatedAmenity = amenityRepository.save(existingAmenity);
        return amenityMapper.toResponse(updatedAmenity);
    }

    @Override
    public AmenityResponse toggleActiveStatus(UUID id) {
        Amenity amenity = amenityRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));

        // Toggle the boolean value
        amenity.setActive(!amenity.isActive());

        Amenity updatedAmenity = amenityRepository.save(amenity);
        return amenityMapper.toResponse(updatedAmenity);
    }

    @Override
    public AmenityResponse toggleFeaturedStatus(UUID id) {
        Amenity amenity = amenityRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));

        amenity.setFeatured(!amenity.isFeatured());

        Amenity updatedAmenity = amenityRepository.save(amenity);
        return amenityMapper.toResponse(updatedAmenity);
    }

    @Override
    public void deleteAmenity(UUID id) {

        Amenity existingAmenity = amenityRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BadRequestException("Amenity not found"));

        existingAmenity.softDelete();
        amenityRepository.save(existingAmenity);

    }

}
