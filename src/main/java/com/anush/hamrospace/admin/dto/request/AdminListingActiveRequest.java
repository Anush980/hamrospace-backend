package com.anush.hamrospace.admin.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminListingActiveRequest(
        @NotNull(message = "Active flag is required") Boolean active
) {
}
