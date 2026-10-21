package com.anush.hamrospace.core.config.email;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix="app.email")
public class ResendEmailProperties {

    private final String fromAddress;
    private final String fromName;
    private final String resendApiKey;

}
