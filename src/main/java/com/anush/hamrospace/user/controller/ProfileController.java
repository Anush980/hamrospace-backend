package com.anush.hamrospace.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.user.dto.request.ProfileRequest;
import com.anush.hamrospace.user.dto.response.UserProfileDetailedResponse;
import com.anush.hamrospace.user.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION+ApiPath.PROFILE+"/me")
public class ProfileController {

    private final ProfileService profileService;
    
    @GetMapping
    public ApiResponse<UserProfileDetailedResponse> getMyProfile(){
        return ApiResponse.ok("Profile fetched successfully",profileService.getMyProfile());
    }

    @PostMapping
    public ApiResponse<UserProfileDetailedResponse> updateMyProfile(ProfileRequest request){
        return ApiResponse.ok("Profile updated successfully",profileService.updateMyProfile(request));
    }

    
}
