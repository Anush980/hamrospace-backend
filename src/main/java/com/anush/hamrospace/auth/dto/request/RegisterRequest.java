package com.anush.hamrospace.auth.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") String fullName,

        @Email(message = "Valid email is required") @NotBlank(message = "Email is required") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, max = 50, message = "Password must be at least 8 characters") String password,

        @AssertTrue(message = "You must accept the terms and conditions") Boolean termsAccepted

) {

}
