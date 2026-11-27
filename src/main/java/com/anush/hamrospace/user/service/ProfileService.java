package com.anush.hamrospace.user.service;

import com.anush.hamrospace.common.storage.dto.request.PresignedUploadRequest;
import com.anush.hamrospace.common.storage.dto.response.PresignedUploadResponse;
import com.anush.hamrospace.user.dto.request.ProfileRequest;
import com.anush.hamrospace.user.dto.response.UserProfileDetailedResponse;
import com.anush.hamrospace.user.dto.response.UserProfileSummaryResponse;

public interface ProfileService {
    
    UserProfileDetailedResponse getMyProfile();

    UserProfileDetailedResponse updateMyProfile(ProfileRequest request);

    UserProfileSummaryResponse getMyProfileSummary();

    PresignedUploadResponse generateProfileImageUploadUrl(PresignedUploadRequest request);

    UserProfileDetailedResponse confirmProfileImageUpload(String objectKey);

    UserProfileDetailedResponse removeProfileImage();
}
