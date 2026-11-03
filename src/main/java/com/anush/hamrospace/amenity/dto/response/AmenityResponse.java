package com.anush.hamrospace.amenity.dto.response;

import java.util.UUID;

public record AmenityResponse(
    UUID id,
    String name,
    String description,
    String iconUrl,
    boolean featured,
    boolean active
) {
    
}
