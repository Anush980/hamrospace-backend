package com.anush.hamrospace.core.email.dto.request;

import java.util.Map;

public record SendEmailRequest(
        String to,
        String from,
        String subject,
        String templateName,
        Map<String, Object> variables) {

}
