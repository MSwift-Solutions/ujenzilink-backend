package com.ujenzilink.ujenzilink_backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class R2StorageConfig {

    private final R2StorageProperties r2Props;

    public R2StorageConfig(R2StorageProperties r2Props) {
        this.r2Props = r2Props;
    }

    @Bean
    public S3Client r2S3Client() {
        validateProperties();

        // Fully configurable endpoint from application.yml / env
        String endpoint = r2Props.endpoint();

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto")) // Cloudflare recommends "auto"
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        r2Props.accessKey(),
                                        r2Props.secretKey()
                                )
                        )
                )
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)   // Required for R2 endpoint
                        .chunkedEncodingEnabled(false)  // Avoid signature errors
                        .build()
                )
                .build();
    }

    private void validateProperties() {
        if (isBlank(r2Props.accountId())) throw missing("r2.account-id");
        if (isBlank(r2Props.accessKey())) throw missing("r2.access-key");
        if (isBlank(r2Props.secretKey())) throw missing("r2.secret-key");
        if (isBlank(r2Props.bucketName())) throw missing("r2.bucket-name");
        if (isBlank(r2Props.publicUrl())) throw missing("r2.public-url");
        if (isBlank(r2Props.endpoint())) throw missing("r2.endpoint");
    }

    private static boolean isBlank(String v) { return v == null || v.isBlank(); }

    private static IllegalStateException missing(String key) {
        return new IllegalStateException("R2 configuration missing: " + key);
    }
}