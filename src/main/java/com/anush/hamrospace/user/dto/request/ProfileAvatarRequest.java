package com.anush.hamrospace.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProfileAvatarRequest(

@NotBlank(message = "File name is required")
        String fileName,

        @NotBlank(message = "Content type is required")
        String contentType,

        @Positive(message = "File size must be greater than 0")
        long size
        
) {
    
}
