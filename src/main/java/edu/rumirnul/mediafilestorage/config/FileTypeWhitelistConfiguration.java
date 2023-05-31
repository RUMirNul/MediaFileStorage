package edu.rumirnul.mediafilestorage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Класс конфигурации белого списка расширений файлов.
 * @author Alexey Svistunov
 * @version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "app.file")
@Getter
@Setter
public class FileTypeWhitelistConfiguration {
    private List<String> extensions;
}