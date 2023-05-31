package edu.rumirnul.mediafilestorage.dto;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

/**
 * Класс DTO для файла. Содержит информацию о файле и содержимое файла.
 * @author Alexey Svistunov
 * @version 1.0
 */
@Data
@Builder
public class FileDto {
    /** Содержимое файла. */
    private InputStream inputStream;
    /** Имя файла. */
    private String name;
    /** Размер содержимого файла */
    private long size;
}