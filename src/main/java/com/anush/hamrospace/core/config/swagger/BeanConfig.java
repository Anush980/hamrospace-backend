package com.anush.hamrospace.core.config.swagger;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Configuration
public class BeanConfig {
    
    @Bean
    public SecureRandom secureRandom(){
        return new SecureRandom();
    }

    @Bean
public ObjectMapper objectMapper() {
    return JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
}
}
