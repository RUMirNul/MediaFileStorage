package edu.rumirnul.mediafilestorage.config;

import edu.rumirnul.mediafilestorage.handler.AsyncExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Класс конфигурации для асинхронных методов Spring.
 * @author Alexey Svistunov
 * @version 1.0
 */
@Profile("!miniointegrationtest")
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfiguration extends AsyncConfigurerSupport {

    private final AsyncThreaderConfiguration asyncThreaderConfiguration;

    /**
     * Устанавливает настройки для ThreadPoolTaskExecutor.
     * @return Возвращает настроенный ThreadPoolTaskExecutor.
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncThreaderConfiguration.getCorePoolSize());
        executor.setMaxPoolSize(asyncThreaderConfiguration.getMaxPoolSize());
        executor.setQueueCapacity(asyncThreaderConfiguration.getQueueCapacity());
        executor.setThreadNamePrefix(asyncThreaderConfiguration.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(asyncThreaderConfiguration.getWaitForTasksToCompileOnShutdown());
        executor.initialize();

        return executor;
    }

    /**
     * Заменяет стандартный handler для ошибок из асинхронных методов SimpleAsyncUncaughtExceptionHandler
     * на собственный handler.
     * @return Возвращает собственный handler для ошибок из асинхронных методов.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }
}
