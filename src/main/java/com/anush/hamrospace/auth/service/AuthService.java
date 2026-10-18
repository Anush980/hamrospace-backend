package com.anush.hamrospace.auth.service;

import com.anush.hamrospace.auth.dto.request.ChangePasswordRequest;
import com.anush.hamrospace.auth.dto.request.ForgotPasswordRequest;
import com.anush.hamrospace.auth.dto.request.LoginRequest;
import com.anush.hamrospace.auth.dto.request.RegisterRequest;
import com.anush.hamrospace.auth.dto.request.ResetPasswordRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;

public interface AuthService {
    
    AuthResponse login(LoginRequest loginRequest);

    AuthResponse register(RegisterRequest registerRequest);

    void changePassword(ChangePasswordRequest changePasswordRequest);

    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
