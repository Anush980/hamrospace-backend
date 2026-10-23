package com.anush.hamrospace.core.email.service;

import com.anush.hamrospace.core.email.dto.request.SendEmailRequest;

public interface EmailService {

    boolean sendEmail(SendEmailRequest request);
    
}
