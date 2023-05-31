package edu.rumirnul.mediafilestorage.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Класс ответа клиенту, содержащий оригинальное имя файла.
 * @author Alexey Svistunov
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class FileDataResponse {
    /** Имя файла. */
    private String originalFileName;
}
