package com.anush.hamrospace.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.auth.dto.request.LoginRequest;
import com.anush.hamrospace.auth.dto.request.RefreshTokenRequest;
import com.anush.hamrospace.auth.dto.request.RegisterRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;
import com.anush.hamrospace.auth.service.AuthService;
import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "auth controller")
@RequiredArgsConstructor
@RequestMapping(ApiPath.VERSION + ApiPath.AUTH)
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new account")
    @PostMapping(ApiPath.REGISTER)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.created("Account has been created", authService.register(request));
    }

    @Operation(summary = "Login in to the account")
    @PostMapping(ApiPath.LOGIN)
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful", authService.login(request));
    }

    @Operation(summary = "Log out of the account")
    @PostMapping(ApiPath.LOGOUT)
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        authService.logout(refreshTokenRequest);
        return ApiResponse.ok("Logout successful");
    }
}
