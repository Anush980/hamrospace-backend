package com.anush.hamrospace.amenity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.amenity.dto.request.AmenityFilterRequest;
import com.anush.hamrospace.amenity.dto.response.AmenityResponse;
import com.anush.hamrospace.amenity.service.AmenityService;
import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.common.dto.response.PageResponse;
import com.anush.hamrospace.common.pagination.PaginationRequest;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION + ApiPath.AMENITY)
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    @Operation(summary = "Get all amenities")
    public ApiResponse<PageResponse<AmenityResponse>> getAllAmenities(AmenityFilterRequest filterRequest,
            PaginationRequest paginationRequest) {
        return ApiResponse.ok("Amenity created", amenityService.getAllAmenities(filterRequest, paginationRequest));
    }
    
}
