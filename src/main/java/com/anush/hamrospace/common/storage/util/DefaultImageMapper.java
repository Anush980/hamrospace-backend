package com.anush.hamrospace.common.storage.util;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import com.anush.hamrospace.common.storage.service.StorageService;
import com.anush.hamrospace.core.config.storage.StorageProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefaultImageMapper {

public static final String ALWAYS_RESOLVE_IMAGE = "java(true)";

    private String avatar = "defaults/avatar.png";
    
    public final StorageService storageService;

    @Named("toAvatarImageUrl")
    public String toAvatarImageUrl(String relativePath) {
        return storageService.getFullUrl(relativePath, avatar);
    }
}
