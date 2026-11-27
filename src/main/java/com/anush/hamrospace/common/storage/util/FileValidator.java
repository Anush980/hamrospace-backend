package com.anush.hamrospace.common.storage.util;

import com.anush.hamrospace.common.storage.dto.request.PresignedUploadRequest;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FileValidator {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    private static final Map<String, Set<String>> MIME_TO_EXTENSIONS = Map.of(
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "image/webp", Set.of("webp")
    );

    private static final Map<String, String> MIME_TO_CANONICAL_EXT = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    public String validateAndGenerateName(PresignedUploadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Upload request must not be null");
        }
        if (request.contentType() == null || request.contentType().isBlank()) {
            throw new IllegalArgumentException("Content type must not be blank");
        }

        String normalizedMime = request.contentType().trim().toLowerCase(Locale.ROOT);
        Set<String> allowedExtensions = MIME_TO_EXTENSIONS.get(normalizedMime);
        if (allowedExtensions == null) {
            throw new IllegalArgumentException("Unsupported content type: " + request.contentType());
        }

        if (request.size() <= 0 || request.size() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size must be between 1 byte and 5 MB");
        }

        String extension = extractAndValidateExtension(request.fileName(), allowedExtensions, normalizedMime);
        return UUID.randomUUID() + "." + extension;
    }

    private String extractAndValidateExtension(
            String fileName,
            Set<String> allowedExtensions,
            String normalizedMime
    ) {
        if (fileName == null || fileName.isBlank()) {
            return MIME_TO_CANONICAL_EXT.get(normalizedMime);
        }

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("File name must include a valid extension");
        }

        String ext = fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!allowedExtensions.contains(ext)) {
            throw new IllegalArgumentException(
                    "File extension '." + ext + "' does not match content type '" + normalizedMime + "'"
            );
        }

        return "jpeg".equals(ext) ? "jpg" : ext;
    }
}