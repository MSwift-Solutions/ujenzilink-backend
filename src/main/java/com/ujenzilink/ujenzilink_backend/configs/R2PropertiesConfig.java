package com.ujenzilink.ujenzilink_backend.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties(R2StorageProperties.class)
public class R2PropertiesConfig {
    // This just enables binding of the record to application.yml/properties
}