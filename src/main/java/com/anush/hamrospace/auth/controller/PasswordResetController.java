package com.anush.hamrospace.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.auth.dto.request.ChangePasswordRequest;
import com.anush.hamrospace.auth.dto.request.ForgotPasswordRequest;
import com.anush.hamrospace.auth.dto.request.ResetPasswordRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;
import com.anush.hamrospace.auth.service.PasswordResetService;
import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION + ApiPath.AUTH)
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "Change your password using old password")
    @PostMapping(ApiPath.CHANGE_PASSWORD)
    public ApiResponse<AuthResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {

        return ApiResponse.ok("Your password has been changed successfully",
                passwordResetService.changePassword(request));
    }

    @Operation(summary = "Send your forgot password otp email")
    @PostMapping(ApiPath.FORGOT_PASSWORD)
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ApiResponse.ok("If the email exists, a reset link has been sent");
    }

    @Operation(summary = "Reset password using token")
    @PostMapping(ApiPath.RESET_PASSWORD)
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ApiResponse.ok("Your password has been changed successfully");
    }
}
