package com.anush.hamrospace.auth.service;

import com.anush.hamrospace.auth.dto.request.LoginRequest;
import com.anush.hamrospace.auth.dto.request.RefreshTokenRequest;
import com.anush.hamrospace.auth.dto.request.RegisterRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    void logout(RefreshTokenRequest refreshTokenRequest);

    // void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    // void resetPassword(ResetPasswordRequest resetPasswordRequest);

    

}
