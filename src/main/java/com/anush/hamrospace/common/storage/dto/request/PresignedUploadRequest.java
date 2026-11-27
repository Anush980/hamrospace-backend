package com.anush.hamrospace.common.storage.dto.request;

public record PresignedUploadRequest(
    String fileName,
    String contentType,
    long size
) {
    
}
