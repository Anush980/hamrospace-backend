package com.anush.hamrospace.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.auth.service.EmailVerificationService;
import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION + ApiPath.AUTH)
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping(ApiPath.RESEND_VERIFICATION)
    public ApiResponse<Void> resendEmailVerify() {
        emailVerificationService.resendVerificationEmail();
        return ApiResponse.ok("verification email sent");
    }

    @PostMapping(ApiPath.VERIFY_EMAIL)
    public ApiResponse<Void> verifyEmail(@RequestParam String token){
        emailVerificationService.verifyEmail(token);
        return ApiResponse.ok("Your email has been verified");
    }


}
