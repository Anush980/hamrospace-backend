package com.anush.hamrospace.user.service;

import org.springframework.web.multipart.MultipartFile;

import com.anush.hamrospace.user.dto.request.ProfileRequest;
import com.anush.hamrospace.user.dto.response.UserProfileDetailedResponse;
import com.anush.hamrospace.user.dto.response.UserProfileSummaryResponse;

public interface ProfileService {
    
    UserProfileDetailedResponse getMyProfile();

    UserProfileDetailedResponse updateMyProfile(ProfileRequest request);

    UserProfileSummaryResponse getMySummaryProfile();

    UserProfileDetailedResponse uploadProfileImage(MultipartFile file);
    UserProfileDetailedResponse removeProfileImage();
}
