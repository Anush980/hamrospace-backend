package com.anush.hamrospace.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @Email(message = "valid Email is required") @NotBlank(message = "email is required") String email,

        @NotBlank(message = "Token is required") String token,

        @NotBlank(message = "New password is required") @Size(min = 8, max = 50, message = "New password must be at least 8 characters") String newPassword) {
}
