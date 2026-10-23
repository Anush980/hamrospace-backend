package com.anush.hamrospace.common.storage.service.impl;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.anush.hamrospace.common.storage.service.StorageService;
import com.anush.hamrospace.common.storage.util.StorageHelper;
import com.anush.hamrospace.core.config.storage.StorageProperties;
import com.anush.hamrospace.core.exception.http.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
public class S3StorageServiceImpl implements StorageService {

    private final S3Client s3Client;
    private final StorageProperties.S3Properties storageProperties;
    private final StorageHelper storageHelper;
    private final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public String upload(MultipartFile file, String folderPath) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }

        String fileName = storageHelper.validateAndGenerateFileName(file.getOriginalFilename());


        String cleanFolder = storageHelper.sanitizeFolder(folderPath);
        String objectKey = cleanFolder.isEmpty() ? fileName : cleanFolder + "/" + fileName;
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(storageProperties.getBucketName())
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Successfully uploaded file to Cloudflare R2 with key: {}", objectKey);
            return objectKey;
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudflare R2", e);
            throw new RuntimeException("Could not store file in Cloudflare R2 storage", e);
        }
    }

    @Override
    public String replace(String oldRelativePath, MultipartFile newFile, String folderPath) {
        if (oldRelativePath != null && !oldRelativePath.isBlank()) {
            delete(oldRelativePath);
        }
        return upload(newFile, folderPath);
    }

    @Override
    public void delete(String relativePath) {
       if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(storageProperties.getBucketName())
                    .key(relativePath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file from Cloudflare R2 with key: {}", relativePath);
        } catch (Exception e) {
            log.error("Failed to delete file from Cloudflare R2 with key: {}", relativePath, e);
        }
    }

 @Override
    public String getFullUrl(String relativePath, String defaultRelativePath) {
        String pathToUse = (relativePath != null && !relativePath.isBlank()) 
                ? relativePath 
                : defaultRelativePath;

        if (pathToUse == null || pathToUse.isBlank()) {
            return null;
        }

        // If the path is already a full external URL (e.g. static fallback in R2/CDN), return as-is
        if (pathToUse.startsWith("http://") || pathToUse.startsWith("https://")) {
            return pathToUse;
        }

        String publicDomain = storageProperties.getPublicDomain();
        if (publicDomain.endsWith("/")) {
            publicDomain = publicDomain.substring(0, publicDomain.length() - 1);
        }

        String cleanPath = pathToUse.startsWith("/") ? pathToUse.substring(1) : pathToUse;
        return publicDomain + "/" + cleanPath;
    }

}
