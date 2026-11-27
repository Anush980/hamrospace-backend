package com.anush.hamrospace.common.storage.service.impl;
import com.anush.hamrospace.common.storage.dto.request.PresignedUploadRequest;
import com.anush.hamrospace.common.storage.dto.response.PresignedUploadResponse;
import com.anush.hamrospace.common.storage.service.PresignStorageService;
import com.anush.hamrospace.common.storage.util.FileValidator;
import com.anush.hamrospace.core.config.storage.StorageProperties;

import java.time.Duration;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class PresignStorageServiceImpl implements PresignStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileValidator fileValidator;
    private final StorageProperties properties;

    @Override
    public PresignedUploadResponse presignUpload(PresignedUploadRequest request, String folderPrefix) {
        String safeFileName = fileValidator.validateAndGenerateName(request);
        String normalizedContentType = request.contentType().trim().toLowerCase(Locale.ROOT);
        String objectKey = buildObjectKey(folderPrefix, safeFileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getS3().getBucketName())
                .key(objectKey)
                .contentType(normalizedContentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.getPresignExpirationDuration()))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUploadResponse(
                presigned.url().toString(),
                objectKey,
                joinUrl(properties.getS3().getPublicDomain(), objectKey),
                properties.getPresignExpirationDuration()
        );
    }

    @Override
    public boolean objectExists(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return false;
        }
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.getS3().getBucketName())
                    .key(objectKey)
                    .build());
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.getS3().getBucketName())
                .key(objectKey)
                .build());
    }

    private String buildObjectKey(String folderPrefix, String safeFileName) {
        if (folderPrefix == null || folderPrefix.isBlank()) {
            return safeFileName;
        }
        String cleanPrefix = folderPrefix.trim().replaceAll("^/+|/+$", "");
        return cleanPrefix + "/" + safeFileName;
    }

    private String joinUrl(String baseUrl, String objectKey) {
        String cleanBase = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        String cleanKey = objectKey.replaceAll("^/+", "");
        return cleanBase + "/" + cleanKey;
    }
}