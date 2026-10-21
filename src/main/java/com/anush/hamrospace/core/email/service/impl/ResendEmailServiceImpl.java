package com.anush.hamrospace.core.email.service.impl;

import org.springframework.stereotype.Service;

import com.anush.hamrospace.core.email.dto.request.SendEmailRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResendEmailServiceImpl implements EmailService {
    
    @Override
    public boolean sendEmail(SendEmailRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEmail'");
    }
    
}
