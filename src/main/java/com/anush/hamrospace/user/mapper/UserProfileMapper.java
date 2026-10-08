package com.anush.hamrospace.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.anush.hamrospace.user.dto.request.ProfileRequest;
import com.anush.hamrospace.user.dto.response.UserProfileDetailedResponse;
import com.anush.hamrospace.user.dto.response.UserProfileSummaryResponse;
import com.anush.hamrospace.user.entity.User;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserProfileMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.profile.fullName")
    @Mapping(target = "profileImageUrl", source = "user.profile.profileImageUrl")
    @Mapping(target = "preferredLanguage", source = "user.profile.preferredLanguage")
    @Mapping(target = "uiTheme", source = "user.profile.uiTheme")
    UserProfileSummaryResponse toSummaryResponse(User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.profile.fullName")
    @Mapping(target = "phoneNumber", source = "user.profile.phoneNumber")
    @Mapping(target = "addressType", source = "user.profile.addressType")
    @Mapping(target = "address", source = "user.profile.address")
    @Mapping(target = "profileImageUrl", source = "user.profile.profileImageUrl")
    @Mapping(target = "bio", source = "user.profile.bio")
    @Mapping(target = "preferredLanguage", source = "user.profile.preferredLanguage")
    @Mapping(target = "uiTheme", source = "user.profile.uiTheme")
    @Mapping(target = "subscriptionType",source = "user.subscriptionType")
    @Mapping(target = "isEmailVerified", source = "user.emailVerified")
    @Mapping(target = "createdAt", source = "user.profile.createdAt")
    @Mapping(target = "updatedAt", source = "user.profile.updatedAt")
    UserProfileDetailedResponse toDetailedResponse(User user);


    User toEntity(ProfileRequest request);
}
