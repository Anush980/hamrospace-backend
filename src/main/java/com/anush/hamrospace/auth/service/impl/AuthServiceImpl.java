package com.anush.hamrospace.auth.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.auth.dto.request.ChangePasswordRequest;
import com.anush.hamrospace.auth.dto.request.ForgotPasswordRequest;
import com.anush.hamrospace.auth.dto.request.LoginRequest;
import com.anush.hamrospace.auth.dto.request.RegisterRequest;
import com.anush.hamrospace.auth.dto.request.ResetPasswordRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;
import com.anush.hamrospace.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changePassword'");
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'forgotPassword'");
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetPassword'");
    }
    
}
