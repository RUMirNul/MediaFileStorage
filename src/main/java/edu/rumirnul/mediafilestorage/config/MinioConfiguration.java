package edu.rumirnul.mediafilestorage.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Класс конфигурации minio хранилища файлов.
 * @author Alexey Svistunov
 * @version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "app.minio")
@Getter
@Setter
public class MinioConfiguration {
    /** URL хранилища. */
    private String url;
    /** Имя ведра для хранения файлов. */
    private String bucket;
    /** Логин для доступа к хранилищу файлов. */
    private String accessKey;
    /** Пароль для доступа к хранилищу файлов. */
    private String secretKey;

    /**
     * Bean minio клиента для доступа к хранилищу файлов.
     * @return Возвращает MinioClient для доступа к хранилищу файлов.
     */
    @Bean
    @Primary
    public MinioClient minionClient() {
        return new MinioClient.Builder()
                .credentials(accessKey, secretKey)
                .endpoint(url)
                .build();
    }
}
