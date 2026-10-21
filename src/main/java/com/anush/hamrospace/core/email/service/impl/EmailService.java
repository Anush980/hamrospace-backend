package com.anush.hamrospace.core.email.service.impl;

import com.anush.hamrospace.core.email.dto.request.SendEmailRequest;

public interface EmailService {

    boolean sendEmail(SendEmailRequest request);
    
}
