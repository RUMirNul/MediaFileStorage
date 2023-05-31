package edu.rumirnul.mediafilestorage.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Класс ответа клиенту, содержащий уникальный идентификатор файла.
 * @author Alexey Svistunov
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class FileUploadResponse {
    /** Уникальный идентификатор. */
    private Long id;
}
