package edu.rumirnul.mediafilestorage.handler;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Утилитарный класс.
 * @author Alexey Svistunov
 * @version 1.0
 */
public final class ExceptionHandlerUtils {
    public ExceptionHandlerUtils() {
    }

    /**
     * Обрабатывает исключение и формирует текстовое сообщение об ошибке.
     * @param t исключение.
     * @return Возвращает имя класса исключения и содержащиеся сообщение.
     */
    public static String buildErrorMessage(Throwable t) {
        StringBuilder message = new StringBuilder(ExceptionUtils.getMessage(t));

        Throwable cause;
        if ((cause = t.getCause()) != null) {
            message.append(", cause: ").append(ExceptionUtils.getMessage(cause));
        }

        return message.toString();
    }
}
