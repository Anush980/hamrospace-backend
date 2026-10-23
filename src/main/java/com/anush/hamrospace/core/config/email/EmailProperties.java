package com.anush.hamrospace.core.config.email;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix="app.email")
public class EmailProperties {

    private String provider;
    private String fromAddress;
    private String fromName;

    private ResendProperties resend = new ResendProperties();

    @Getter
    @Setter
    public static class ResendProperties {
        private String apiKey;
    }

}
