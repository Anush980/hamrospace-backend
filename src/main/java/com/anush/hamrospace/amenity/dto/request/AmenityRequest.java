package com.anush.hamrospace.amenity.dto.request;

public record AmenityRequest(
     String name,
    String description,
    String iconUrl,
    Boolean featured,
    Boolean active
)
 {}
