package com.anush.hamrospace.core.email.service;

import java.util.Map;

public interface EmailTemplateService {
    
    String render(String templateName, Map<String, Object> variables);
    void clearCache();
    String loadTemplate(String templateName);
}
