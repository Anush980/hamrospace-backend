package com.anush.hamrospace.core.email.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.anush.hamrospace.core.email.service.EmailTemplateService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final ConcurrentHashMap<String, String> templateCache = new ConcurrentHashMap<>();

    @Override
    public String render(String templateName, Map<String, Object> variables) {
        String content = templateCache.computeIfAbsent(templateName, this::loadTemplate);

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            content = content.replace("{{" + entry.getKey() + "}}", value);
        }

        // Remove any leftover unreplaced placeholders
        return content.replaceAll("\\{\\{[^}]+\\}\\}", "");
    }

    @Override
    public void clearCache() {
        templateCache.clear();
    }

    @Override
    public String loadTemplate(String templateName) {
        String path = "templates/email/" + templateName;
        try {
            ClassPathResource resource = new ClassPathResource(path);
            byte[] bytes = resource.getInputStream().readAllBytes();
            log.debug("Loaded email template: {}", path);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Email template not found: {}", path);
            throw new RuntimeException("Email template not found: " + templateName, e);
        }
    }

}
