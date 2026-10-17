package com.anush.hamrospace.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.anush.hamrospace.user.dto.request.ProfileRequest;
import com.anush.hamrospace.user.dto.response.UserProfileDetailedResponse;
import com.anush.hamrospace.user.dto.response.UserProfileSummaryResponse;
import com.anush.hamrospace.user.mapper.UserProfileMapper;
import com.anush.hamrospace.user.repository.ProfileRepository;
import com.anush.hamrospace.user.repository.UserRepository;
import com.anush.hamrospace.user.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class ProfileServiceImpl implements ProfileService{

        private final UserRepository userRepository;
        private final ProfileRepository profileRepository;
        private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfileDetailedResponse getMyProfile() {
        


        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMyProfile'");
    }

    @Override
    public UserProfileDetailedResponse updateMyProfile(ProfileRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateMyProfile'");
    }

    @Override
    public UserProfileSummaryResponse getMySummaryProfile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMySummaryProfile'");
    }

    @Override
    public UserProfileDetailedResponse uploadProfileImage(MultipartFile file) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadProfileImage'");
    }

    @Override
    public UserProfileDetailedResponse removeProfileImage() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeProfileImage'");
    }
    
}
