package com.anush.hamrospace.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.anush.hamrospace.common.mapper.MapStructMapperHelper;
import com.anush.hamrospace.common.storage.util.DefaultImageMapper;
import com.anush.hamrospace.user.dto.request.ProfileRequest;
import com.anush.hamrospace.user.dto.response.UserProfileDetailedResponse;
import com.anush.hamrospace.user.dto.response.UserProfileSummaryResponse;
import com.anush.hamrospace.user.entity.Profile;
import com.anush.hamrospace.user.entity.User;

@Mapper(componentModel = "spring",uses = {DefaultImageMapper.class,MapStructMapperHelper.class},nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserProfileMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(source = "profile.profileImageUrl",target = "profileImageUrl", qualifiedByName = "toAvatarImageUrl",conditionExpression = DefaultImageMapper.ALWAYS_RESOLVE_IMAGE)
    UserProfileSummaryResponse toSummaryResponse(User user,Profile profile);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "createdAt", source = "profile.createdAt")
    @Mapping(target = "updatedAt", source = "profile.updatedAt")
     @Mapping(source = "profile.profileImageUrl",target = "profileImageUrl", qualifiedByName = "toAvatarImageUrl",conditionExpression = DefaultImageMapper.ALWAYS_RESOLVE_IMAGE)
    @Mapping(target = "isEmailVerified",ignore = true)
    UserProfileDetailedResponse toDetailedResponse(User user,Profile profile);


    @Mapping(target = "id", ignore = true)
    User toEntity(ProfileRequest request);

    @Mapping(target = "id", ignore = true)
    void updateProfile(ProfileRequest request,@MappingTarget Profile profile);
}
