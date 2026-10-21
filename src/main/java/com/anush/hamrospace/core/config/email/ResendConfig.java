package com.anush.hamrospace.core.config.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resend.Resend;

@Configuration
public class ResendConfig {
    
    @Bean
    public Resend resendClient(ResendEmailProperties emailProperties){
        return new Resend(emailProperties.getResendApiKey());
    }
}
