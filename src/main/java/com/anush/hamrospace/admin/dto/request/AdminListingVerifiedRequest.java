package com.anush.hamrospace.admin.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminListingVerifiedRequest(
        @NotNull(message = "Verified flag is required") Boolean verified
) {
}
