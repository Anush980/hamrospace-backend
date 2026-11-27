package com.anush.hamrospace.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmProfileImageRequest(

    @NotBlank(message = "Object key is required")
    String objectKey
) {
    
}
