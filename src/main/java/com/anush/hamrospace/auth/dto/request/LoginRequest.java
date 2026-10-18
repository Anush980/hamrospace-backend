package com.anush.hamrospace.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Email(message = "valid Email is required") @NotBlank(message = "email is required") String email,
        @NotBlank(message = "password is required") @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters") String password,
        Boolean rememberMe
    ) {

}
