package com.anush.hamrospace.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
}
