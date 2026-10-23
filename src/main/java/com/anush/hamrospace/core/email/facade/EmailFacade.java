package com.anush.hamrospace.core.email.facade;
import java.time.Year;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.core.config.AppProperties;
import com.anush.hamrospace.core.email.dto.request.SendEmailRequest;
import com.anush.hamrospace.core.email.service.EmailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailFacade {

    private final AppProperties appProperties;
    private final EmailService emailService;

    @Async
    public void sendWelcomeEmail(String email, String fullName, String token) {

        String verifyUrl = appProperties.getFrontendUrl() + ApiPath.VERIFY_EMAIL + "?token=" + token;

        SendEmailRequest request = new SendEmailRequest(
                email,
                null,
                "Welcome to " + appProperties.getName(),
                "welcome/welcome.html",
                Map.of(
                        "appName", appProperties.getName(),
                        "name", extractFirstName(fullName, email),
                        "otp", token,
                        "otpExpiryMinutes", String.valueOf(appProperties.getOtpExpiryMinutes()),
                        "verifyUrl", verifyUrl,
                        "exploreUrl", appProperties.getFrontendUrl() + "/explore",
                        "shopUrl", appProperties.getFrontendUrl() + "/shop",
                        "dashboardUrl", appProperties.getFrontendUrl() + "/dashboard",
                        "supportUrl", appProperties.getFrontendUrl() + "/support",
                        "year", currentYear()));

        emailService.sendEmail(request);
    }

    @Async
    public void sendForgotPasswordEmail(String email, String fullName, String token) {

        SendEmailRequest request = new SendEmailRequest(
                email,
                null,
                "Reset your " + appProperties.getName() + " password",
                "auth/forgot-password.html",
                Map.of(
                        "appName", appProperties.getName(),
                        "name", extractFirstName(fullName, email),
                        "otp", token,
                        "otpExpiryMinutes", appProperties.getOtpExpiryMinutes(),
                        "supportUrl", appProperties.getFrontendUrl() + "/support",
                        "year", currentYear()));
        emailService.sendEmail(request);

    }

    @Async
    public void sendPasswordChangedEmail(String email, String fullName, String deviceInfo, String ipAddress) {
        SendEmailRequest request = new SendEmailRequest(
                email,
                null,
                "Your " + appProperties.getName() + " password was changed",
                "auth/password-changed.html",
                Map.of(
                        "appName", appProperties.getName(),
                        "name", extractFirstName(fullName, email),
                        "email", email,
                        "forgotPasswordUrl", appProperties.getFrontendUrl() + "/forgot-password",
                        "deviceInfo", deviceInfo,
                        "ipAddress", ipAddress,
                        "supportUrl", appProperties.getFrontendUrl() + "/support",
                        "year", currentYear()));
        emailService.sendEmail(request);
    }

    @Async
    public void sendVerifyEmail(String email,String fullName, String token) {
        String verifyUrl = appProperties.getFrontendUrl() + ApiPath.VERIFY_EMAIL + "?token=" +token;
        SendEmailRequest request = new SendEmailRequest(
                email,
                null,
                "Verify your " + appProperties.getName() + " email address",
                "auth/verify-email.html",
                Map.of(
                        "appName", appProperties.getName(),
                        "name", extractFirstName(fullName, email),
                        "otp", token,
                        "otpExpiryMinutes", appProperties.getOtpExpiryMinutes(),
                        "verifyUrl", verifyUrl,
                        "supportUrl", appProperties.getFrontendUrl() + "/support",
                        "year", currentYear()));
        emailService.sendEmail(request);
    }

    private String extractFirstName(String fullName, String email) {
        if (fullName != null && !fullName.isBlank()) {
            return fullName.trim().split(" ")[0];
        }
        if (email != null && !email.isBlank()) {
            return email.split("@")[0];
        }
        return "User";
    }

    private String currentYear() {
        return String.valueOf(Year.now().getValue());
    }
}
