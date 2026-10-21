package com.anush.hamrospace.core.config.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    
    private final String accessKey;

    private final String refreshKey;

    private final Duration accessExpiration;

    private final Duration refreshExpiration;

    private final Duration shortRefreshExpiration;

    private final String issuer;

    
}
