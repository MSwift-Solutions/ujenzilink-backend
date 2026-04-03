package com.ujenzilink.ujenzilink_backend.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /** Primary executor for all async tasks, including R2 operations. */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("r2-async-");
        executor.setRejectedExecutionHandler((runnable, pool) ->
                log.error("R2 async task rejected — queue full. Consider increasing taskExecutor queue capacity."));
        executor.setTaskDecorator(runnable -> () -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                log.error("Uncaught exception in r2-async thread", ex);
            }
        });
        executor.initialize();
        return executor;
    }
}
