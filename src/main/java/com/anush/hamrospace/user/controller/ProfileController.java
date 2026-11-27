package com.anush.hamrospace.user.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.common.storage.dto.request.PresignedUploadRequest;
import com.anush.hamrospace.common.storage.dto.response.PresignedUploadResponse;
import com.anush.hamrospace.user.dto.request.ConfirmProfileImageRequest;
import com.anush.hamrospace.user.dto.request.ProfileRequest;
import com.anush.hamrospace.user.dto.response.UserProfileDetailedResponse;
import com.anush.hamrospace.user.dto.response.UserProfileSummaryResponse;
import com.anush.hamrospace.user.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION + ApiPath.PROFILE + "/me")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ApiResponse<UserProfileDetailedResponse> getMyProfile() {
        return ApiResponse.ok("Profile fetched successfully", profileService.getMyProfile());
    }

    @GetMapping("/summary")
    public ApiResponse<UserProfileSummaryResponse> getMyProfileSummary() {
        return ApiResponse.ok("Profile fetched successfully", profileService.getMyProfileSummary());
    }

    @PatchMapping
    public ApiResponse<UserProfileDetailedResponse> updateMyProfile(@Valid @RequestBody ProfileRequest request) {
        return ApiResponse.ok("Profile updated successfully", profileService.updateMyProfile(request));
    }

    @Operation(summary = "Generate presigned URL for profile image upload")
    @PostMapping("/image/presign")
    public ApiResponse<PresignedUploadResponse> presignProfileImageUpload(
            @Valid @RequestBody PresignedUploadRequest request) {
        return ApiResponse.ok( "Presigned upload URL generated successfully",profileService.generateProfileImageUploadUrl(request));
    }

    @Operation(summary = "Confirm profile image upload")
    @PostMapping("/image/confirm")
    public ApiResponse<UserProfileDetailedResponse> confirmProfileImageUpload(
            @Valid @RequestBody ConfirmProfileImageRequest request) {
        return ApiResponse.ok( "Profile image uploaded successfully", profileService.confirmProfileImageUpload(request.objectKey()));
    }

    @Operation(summary = "Remove profile image")
    @DeleteMapping("/image")
    public ApiResponse<UserProfileDetailedResponse> removeProfileImage() {
        return ApiResponse.ok("Profile image removed successfully", profileService.removeProfileImage());
    }

}
