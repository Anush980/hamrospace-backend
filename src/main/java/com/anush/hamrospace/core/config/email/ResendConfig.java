package com.anush.hamrospace.core.config.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resend.Resend;

@Configuration
@ConditionalOnProperty(name = "app.email.provider", havingValue = "resend")
public class ResendConfig {

    @Bean
    public Resend resendClient(EmailProperties emailProperties){
        return new Resend(emailProperties.getResend().getApiKey());
    }
}
