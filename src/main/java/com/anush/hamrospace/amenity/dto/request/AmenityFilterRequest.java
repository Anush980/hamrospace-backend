package com.anush.hamrospace.amenity.dto.request;

public record AmenityFilterRequest(
    String search,
    Boolean isFeatured,
    Boolean isActive

) {
    
}
