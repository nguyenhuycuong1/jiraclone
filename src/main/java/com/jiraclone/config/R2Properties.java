package com.jiraclone.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloudflare.r2")
@Getter @Setter
public class R2Properties {
    private String accountId;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String publicUrl;
}
