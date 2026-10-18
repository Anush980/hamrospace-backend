package com.anush.hamrospace.auth.service;

import com.anush.hamrospace.auth.dto.request.RefreshTokenRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;
import com.anush.hamrospace.user.entity.User;

public interface TokenLifeCycleService {

    AuthResponse issueTokenPair(User user, boolean rememberMe);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

}