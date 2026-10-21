package com.anush.hamrospace.auth.service;

public interface EmailVerificationService {

    String generateVerificationToken(String email);

    String validateVerificationToken(String token);

    void resendVerificationEmail();

    void verifyEmail(String token);

}
