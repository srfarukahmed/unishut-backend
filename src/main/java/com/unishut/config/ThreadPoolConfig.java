package com.unishut.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadPoolConfig {

    @Value("${unishut.thread.core-pool-size:5}")
    private int corePoolSize;

    @Value("${unishut.thread.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${unishut.thread.queue-capacity:100}")
    private int queueCapacity;

    @Value("${unishut.thread.keep-alive-seconds:60}")
    private int keepAliveSeconds;


    /**
     * Main asynchronous thread pool for uniShut.
     *
     * This executor will be used for tasks such as:
     *
     * - Processing driver GPS updates
     * - Processing multiple bus-location requests
     * - Reservation processing
     * - Sending notifications
     * - Background tracking operations
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);

        executor.setMaxPoolSize(maxPoolSize);

        executor.setQueueCapacity(queueCapacity);

        executor.setKeepAliveSeconds(keepAliveSeconds);

        executor.setThreadNamePrefix("unishut-worker-");

        executor.setWaitForTasksToCompleteOnShutdown(true);

        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        return executor;
    }
}