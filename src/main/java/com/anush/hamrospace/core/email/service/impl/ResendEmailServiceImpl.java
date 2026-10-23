package com.anush.hamrospace.core.email.service.impl;

import org.springframework.stereotype.Service;

import com.anush.hamrospace.core.config.email.EmailProperties;
import com.anush.hamrospace.core.email.dto.request.SendEmailRequest;
import com.anush.hamrospace.core.email.service.EmailService;
import com.anush.hamrospace.core.email.service.EmailTemplateService;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResendEmailServiceImpl implements EmailService {

    private final Resend resend;
    private final EmailTemplateService emailTemplateService;
    private final EmailProperties emailProperties;

    @Override
    public boolean sendEmail(SendEmailRequest request) {
        try {
            String html = emailTemplateService.render(request.templateName(), request.variables());

            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(emailProperties.getFromName() + " <" + emailProperties.getFromAddress() + ">")
                    .to(request.to())
                    .subject(request.subject())
                    .html(html)
                    .build();
            CreateEmailResponse response = resend.emails().send(options);
            log.info("[RESEND] Email sent to {} | id: {} | subject: {}", request.to(), response.getId(),
                    request.subject());
            return true;
        } catch (Exception e) {
            log.warn("[RESEND] Failed to send to {} | reason: {}", request.to(), e.getMessage());
            return false;
        }
    }

}
