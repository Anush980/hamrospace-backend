package com.anush.hamrospace.amenity.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.amenity.dto.request.AmenityFilterRequest;
import com.anush.hamrospace.amenity.dto.request.AmenityRequest;
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
        return ApiResponse.ok("Amenity fetched successfully", amenityService.getAllAmenities(filterRequest, paginationRequest));
    }
    
    @PostMapping
    @Operation(summary = "Create new amenities")
    public ApiResponse<AmenityResponse> createAmenity(AmenityRequest request){
        return ApiResponse.ok("Amenity created successfully",amenityService.createAmenity(request));
    }

    @PatchMapping(ApiPath.ID)
    @Operation(summary = "Update new amenities")
    public ApiResponse<AmenityResponse> updateAmenity(UUID id,AmenityRequest request){
        return ApiResponse.ok("Amenity updated successfully",amenityService.updateAmenity(id,request));
    }

    @PatchMapping(ApiPath.ID+"/toggle-active")
    public ResponseEntity<AmenityResponse> toggleActiveStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(amenityService.toggleActiveStatus(id));
    }

    @PatchMapping(ApiPath.ID+"/toggle-featured")
    public ResponseEntity<AmenityResponse> toggleFeaturedStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(amenityService.toggleFeaturedStatus(id));
    }

    @DeleteMapping(ApiPath.ID)
    @Operation(summary = "Delete amenity")
    public ApiResponse<Void> deleteAmenity(UUID id){
        return ApiResponse.ok("Amenity deleted successfully",null);
    }
}
