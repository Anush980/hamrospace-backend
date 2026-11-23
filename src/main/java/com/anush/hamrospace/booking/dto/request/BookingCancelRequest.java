package com.anush.hamrospace.booking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BookingCancelRequest(
    
    @NotBlank(message = "Cancellation reason is required")
    String cancellationReason
) {
    
}
