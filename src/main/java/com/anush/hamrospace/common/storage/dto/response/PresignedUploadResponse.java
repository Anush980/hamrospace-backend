package com.anush.hamrospace.common.storage.dto.response;

public record PresignedUploadResponse(
        String uploadUrl,
        String objectKey,
        String publicUrl,
        int expiresInSeconds) {

}
