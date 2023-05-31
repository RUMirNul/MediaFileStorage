package edu.rumirnul.mediafilestorage.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Класс ответа клиенту, если произошла ошибка во время обработки запроса.
 * @author Alexey Svistunov
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class BaseWebResponse {
    /** Сообщение об ошибке. */
    private String errorMessage;
}
