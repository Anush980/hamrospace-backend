package com.anush.hamrospace.listing.dto.response;

import java.util.UUID;

public record ListingImageResponse(
    UUID id,
    String imageUrl,
    String altText,
    boolean primary,
    Integer displayOrder

) {
    
}
