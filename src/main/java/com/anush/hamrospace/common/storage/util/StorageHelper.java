package com.anush.hamrospace.common.storage.util;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.anush.hamrospace.core.config.storage.StorageProperties;
import com.anush.hamrospace.core.exception.ErrorCodes;
import com.anush.hamrospace.core.exception.http.BadRequestException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StorageHelper {

    private final StorageProperties storageProperties;

    public String validateAndGenerateFileName(String fileName) {
        String extension = extractExtension(fileName);
        validateExtension(extension);
        return UUID.randomUUID().toString() + extension;
    }

    public String sanitizeFolder(String folderPath) {
        if (folderPath == null || folderPath.isBlank()) {
            return "";
        }
        String clean = folderPath.trim();
        if (clean.startsWith("/")) clean = clean.substring(1);
        if (clean.endsWith("/")) clean = clean.substring(0, clean.length() - 1);
        return clean;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BadRequestException("Unsupported file format");
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    private void validateExtension(String extension) {
        List<String> allowed = storageProperties.getAllowedExtensions();
        
        // Fallback safety
        if (allowed == null || allowed.isEmpty()) {
            allowed = List.of(".jpg", ".jpeg", ".png"); 
        }

        if (!allowed.contains(extension)) {
            throw new BadRequestException(
                ErrorCodes.BAD_REQUEST, 
                "Unsupported file format '" + extension + "'. Allowed formats: " + allowed
            );
        }
    }
}