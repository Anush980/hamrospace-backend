package com.anush.hamrospace.core.security.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties("app.cors")
public class CorsProperties {
    
    private final List<String> allowedOrigins;
}
