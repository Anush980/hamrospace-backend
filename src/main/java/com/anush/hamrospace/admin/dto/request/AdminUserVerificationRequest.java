package com.anush.hamrospace.admin.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminUserVerificationRequest(
        @NotNull(message = "Verified flag is required") Boolean verified) {
}
