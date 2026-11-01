package com.anush.hamrospace.user.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final StorageService storageService;
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

    @Override
    public UserProfileDetailedResponse uploadProfileImage(MultipartFile file) {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Profile profile = findOrCreateProfile(user);
        if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isBlank()) {
            storageService.delete(profile.getProfileImageUrl());
        }
        String folderPath = storagePathBuilder.userProfilePath(userId);
        String relativePath = storageService.upload(file, folderPath);
        profile.setProfileImageUrl(relativePath);
        Profile updatedProfile = profileRepository.save(profile);
        return userProfileMapper.toDetailedResponse(user, updatedProfile);
    }

    @Override
    public UserProfileDetailedResponse removeProfileImage() {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Profile profile = findOrCreateProfile(user);
        if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isBlank()) {
            storageService.delete(profile.getProfileImageUrl());
            profile.setProfileImageUrl(null);
        }
        Profile updatedProfile = profileRepository.save(profile);
        return userProfileMapper.toDetailedResponse(user, updatedProfile);
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

}
