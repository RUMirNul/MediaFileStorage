package edu.rumirnul.mediafilestorage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для ThreadPoolTaskExecutor.
 * @author Alexey Svistunov
 * @version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "app.threader-pool")
@Getter
@Setter
public class AsyncThreaderConfiguration {
    /** Размер пула потоков для выполнения задач. */
    private int corePoolSize;
    /** Максимальный размер пула потоков для выполнения задач. */
    private int maxPoolSize;
    /** Размер очереди задач. */
    private int queueCapacity;
    /** Префикс имени потока. */
    private String threadNamePrefix;
    /** Ожидать выполнения задач перед остановкой программы*/
    private Boolean waitForTasksToCompileOnShutdown;
}
