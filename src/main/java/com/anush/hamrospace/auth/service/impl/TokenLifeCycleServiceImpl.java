package com.anush.hamrospace.auth.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.auth.dto.request.RefreshTokenRequest;
import com.anush.hamrospace.auth.dto.response.AuthResponse;
import com.anush.hamrospace.auth.service.TokenLifeCycleService;
import com.anush.hamrospace.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenLifeCycleServiceImpl implements TokenLifeCycleService {

    @Override
    public AuthResponse issueTokenPair(User user, boolean rememberMe) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'issueTokenPair'");
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshToken'");
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }
    
}
