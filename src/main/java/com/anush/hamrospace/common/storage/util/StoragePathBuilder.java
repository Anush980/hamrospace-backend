package com.anush.hamrospace.common.storage.util;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class StoragePathBuilder {

    public String userProfilePath(UUID userId) {
        return String.format("users/%s/profile", userId);
    }

    public String listingImagePath(UUID listingId) {
    return "listings/" + listingId + "/images";
}
}
