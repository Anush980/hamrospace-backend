package com.anush.hamrospace.core.config.storage;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix="app.storage")
public class StorageProperties {
    
    private String provider;
    private List<String> allowedExtensions;

    private S3Properties s3 = new S3Properties();

    @Getter
    @Setter
    public static class S3Properties{
        private String region;
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private String publicDomain;
    }
}
