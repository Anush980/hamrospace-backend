package com.anush.hamrospace.common.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    
    String upload(MultipartFile file, String folderPath);

    String replace(String oldRelativePath, MultipartFile newFile, String folderPath);

    void delete(String relativePath);

    String getFullUrl(String relativePath, String defaultRelativePath);
}
