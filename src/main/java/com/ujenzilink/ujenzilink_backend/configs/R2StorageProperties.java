package com.ujenzilink.ujenzilink_backend.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "r2")
public record R2StorageProperties(
        String accountId,
        String accessKey,
        String secretKey,
        String bucketName,
        String publicUrl,
        String endpoint
) {}