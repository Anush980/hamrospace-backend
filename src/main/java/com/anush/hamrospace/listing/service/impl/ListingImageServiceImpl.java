package com.anush.hamrospace.listing.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.anush.hamrospace.core.exception.http.BadRequestException;
import com.anush.hamrospace.core.exception.http.UnauthorizedAccessException;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.listing.dto.response.ListingImageResponse;
import com.anush.hamrospace.listing.entity.Listing;
import com.anush.hamrospace.listing.repository.ListingRepository;
import com.anush.hamrospace.listing.service.ListingImageService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ListingImageServiceImpl implements ListingImageService {

    private final ListingRepository listingRepository;
    private final int MAX_LISTING_IMAGE_COUNT=10;
    
    @Override
    public List<ListingImageResponse> uploadListingImages(String listingId, List<MultipartFile> files) {
        
        UUID userId = SecurityUtil.getCurrentUserId();

        Listing listing = listingRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(()-> new BadRequestException("Listing not found"));

        if (!listing.getHost().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to add images to this listing");
        }

        List<ListingImageResponse> responses = new ArrayList<>();
        int existingCount = listing.getImages().size();
        int newCount = files.size();
        int totalNewImageCount = existingCount+newCount;
        if(totalNewImageCount>MAX_LISTING_IMAGE_COUNT){
            throw new BadRequestException("You can't upload image more than "+MAX_LISTING_IMAGE_COUNT+" on one listing");
        }

        throw new UnsupportedOperationException("Unimplemented method 'uploadListingImages'");
    }

    @Override
    public void removeListingImage(String listingId, String imageId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeListingImage'");
    }

    @Override
    public ListingImageResponse setPrimaryImage(String listingId, String imageId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPrimaryImage'");
    }
    
}
