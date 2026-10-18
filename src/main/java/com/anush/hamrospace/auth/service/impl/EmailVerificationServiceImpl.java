package com.anush.hamrospace.auth.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.auth.service.EmailVerificationService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    @Override
    public String generateVerificationToken(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateVerificationToken'");
    }

    @Override
    public String validateVerificationToken(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateVerificationToken'");
    }

    @Override
    public void resendVerificationEmail(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resendVerificationEmail'");
    }
    
}
