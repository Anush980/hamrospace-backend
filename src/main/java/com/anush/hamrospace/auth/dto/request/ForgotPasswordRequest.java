package com.anush.hamrospace.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    
    @Email(message = "valid Email is required") @NotBlank(message = "email is required") String email
) {
    
}
