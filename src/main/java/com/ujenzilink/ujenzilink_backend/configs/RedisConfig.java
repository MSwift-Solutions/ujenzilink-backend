package com.ujenzilink.ujenzilink_backend.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "com.ujenzilink.ujenzilink_backend.auth.repositories")
public class RedisConfig {
}
