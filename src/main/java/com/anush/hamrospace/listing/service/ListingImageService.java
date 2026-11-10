package com.anush.hamrospace.listing.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.anush.hamrospace.listing.dto.response.ListingImageResponse;

public interface ListingImageService {

    List<ListingImageResponse> uploadListingImages(String listingId, List<MultipartFile> files);

    void removeListingImage(String listingId, String imageId);

    ListingImageResponse setPrimaryImage(String listingId, String imageId);
}
