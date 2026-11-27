package com.anush.hamrospace.user.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.anush.hamrospace.common.storage.dto.request.PresignedUploadRequest;
import com.anush.hamrospace.common.storage.dto.response.PresignedUploadResponse;
import com.anush.hamrospace.common.storage.service.PresignStorageService;
import com.anush.hamrospace.common.storage.service.StorageService;
import com.anush.hamrospace.common.storage.util.StoragePathBuilder;
import com.anush.hamrospace.core.exception.http.ResourceNotFoundException;
import com.anush.hamrospace.core.security.util.SecurityUtil;
import com.anush.hamrospace.user.dto.request.ProfileRequest;
import com.anush.hamrospace.user.dto.response.UserProfileDetailedResponse;
import com.anush.hamrospace.user.dto.response.UserProfileSummaryResponse;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.mapper.UserProfileMapper;
import com.anush.hamrospace.user.repository.ProfileRepository;
import com.anush.hamrospace.user.repository.UserRepository;
import com.anush.hamrospace.user.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PresignStorageService presignStorageService;
    private final StoragePathBuilder storagePathBuilder;
    private final ProfileRepository profileRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfileDetailedResponse getMyProfile() {
        UUID userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Profile profile = findOrCreateProfile(user);

        return userProfileMapper.toDetailedResponse(user, profile);
    }

    @Override
    public UserProfileDetailedResponse updateMyProfile(ProfileRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Profile profile = findOrCreateProfile(user);

        userProfileMapper.updateProfile(request, profile);
        profileRepository.save(profile);
        userRepository.save(user);

        return userProfileMapper.toDetailedResponse(user, profile);
    }

    @Override
    public UserProfileSummaryResponse getMyProfileSummary() {
        UUID userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Profile profile = findOrCreateProfile(user);

        return userProfileMapper.toSummaryResponse(user, profile);
    }

    private Profile findOrCreateProfile(User user) {
        return profileRepository.findByUserAndDeletedAtIsNull(user)
                .orElseGet(() -> {
                    Profile newProfile = new Profile();
                    newProfile.setUser(user);
                    newProfile.setFullName("null");
                    return profileRepository.save(newProfile);
                });
    }

    @Override
    public PresignedUploadResponse generateProfileImageUploadUrl(PresignedUploadRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        String folderPrefix = storagePathBuilder.userProfilePath(userId);
        return presignStorageService.presignUpload(request, folderPrefix);
    }

    @Override
    public UserProfileDetailedResponse confirmProfileImageUpload(String objectKey) {
        UUID userId = SecurityUtil.getCurrentUserId();
        String expectedPrefix = storagePathBuilder.userProfilePath(userId) + "/";

        if (objectKey == null || !objectKey.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException("Invalid object key for current user profile");
        }

        if (!presignStorageService.objectExists(objectKey)) {
            throw new IllegalStateException("Uploaded file not found in storage");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Profile profile = findOrCreateProfile(user);

        String oldObjectKey = profile.getProfileImageUrl();
        profile.setProfileImageUrl(objectKey);
        Profile savedProfile = profileRepository.save(profile);

        if (oldObjectKey != null && !oldObjectKey.isBlank() && !oldObjectKey.equals(objectKey)) {
            presignStorageService.deleteObject(oldObjectKey);
        }

        return userProfileMapper.toDetailedResponse(user, savedProfile);
    }

    @Override
    public UserProfileDetailedResponse removeProfileImage() {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Profile profile = findOrCreateProfile(user);

        String oldObjectKey = profile.getProfileImageUrl();
        profile.setProfileImageUrl(null);
        Profile savedProfile = profileRepository.save(profile);

        if (oldObjectKey != null && !oldObjectKey.isBlank()) {
            presignStorageService.deleteObject(oldObjectKey);
        }

        return userProfileMapper.toDetailedResponse(user, savedProfile);
    }
}
