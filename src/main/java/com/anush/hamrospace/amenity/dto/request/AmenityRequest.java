package com.anush.hamrospace.amenity.dto.request;

public record AmenityRequest(
     String name,
    String description,
    String iconUrl,
    boolean isFeatured,
    boolean isActive
)
 {}
