package com.anush.hamrospace.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private final String name;
    private final String frontendUrl;
    private final String otpExpiryMinutes = "15";
}
