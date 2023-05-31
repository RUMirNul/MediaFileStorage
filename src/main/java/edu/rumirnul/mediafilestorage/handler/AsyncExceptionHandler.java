package edu.rumirnul.mediafilestorage.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

/**
 * Класс handler для отлова исключений в асинхронных методах.
 * @author Alexey Svistunov
 * @version 1.0
 */
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    /**
     * Выполнение работы над отловленным исключением. Исключение логируется.
     * @param ex - исключение.
     * @param method - метод, где возникла ошибка.
     * @param params - параметры метода.
     */
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Method name: {}, Exception message: {}", method.getName(), ex.getMessage());
    }
}