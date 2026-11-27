package com.anush.hamrospace.common.storage.service;

import com.anush.hamrospace.common.storage.dto.request.PresignedUploadRequest;
import com.anush.hamrospace.common.storage.dto.response.PresignedUploadResponse;

public interface PresignStorageService {

    PresignedUploadResponse presignUpload(PresignedUploadRequest request, String folderPrefix);

    boolean objectExists(String objectKey);

    void deleteObject(String objectKey);
}
