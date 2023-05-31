package edu.rumirnul.mediafilestorage.handler;

import edu.rumirnul.mediafilestorage.exception.*;
import edu.rumirnul.mediafilestorage.response.BaseWebResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Класс handler для обработки исключений.
 *
 * @author Alexey Svistunov
 * @version 1.0
 */
@ControllerAdvice
@Slf4j
public class ServiceExceptionHandler {

    /**
     * Отлавливает и обрабатывает ForbiddenFileFormatException.
     *
     * @param exc исключение.
     * @return Возвращает ResponseEntity со статусом UNSUPPORTED_MEDIA_TYPE и сообщением об ошибке.
     */
    @ExceptionHandler(ForbiddenFileFormatException.class)
    public ResponseEntity<BaseWebResponse> handleForbiddenFileFormatException(@NonNull final ForbiddenFileFormatException exc) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new BaseWebResponse(createErrorMessage(exc)));
    }

    /**
     * Отлавливает и обрабатывает FileNotFoundException.
     *
     * @param exc исключение.
     * @return Возвращает ResponseEntity со статусом NOT_FOUND и сообщением об ошибке.
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<BaseWebResponse> handleFileNotFoundException(@NonNull final FileNotFoundException exc) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseWebResponse(createErrorMessage(exc)));
    }

    /**
     * Отлавливает и обрабатывает IOAccessException.
     *
     * @param exc исключение.
     * @return Возвращает ResponseEntity со статусом INTERNAL_SERVER_ERROR и сообщением об ошибке.
     */
    @ExceptionHandler(IOAccessException.class)
    public ResponseEntity<BaseWebResponse> handleIOAccessException(@NonNull final IOAccessException exc) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseWebResponse(createErrorMessage(exc)));
    }

    /**
     * Отлавливает и обрабатывает S3Exception.
     *
     * @param exc исключение.
     * @return Возвращает ResponseEntity со статусом INTERNAL_SERVER_ERROR и сообщением об ошибке.
     */
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<BaseWebResponse> handleS3Exception(@NonNull final S3Exception exc) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseWebResponse(createErrorMessage(exc)));
    }

    /**
     * Отлавливает и обрабатывает NoAccessException.
     *
     * @param exc исключение.
     * @return Возвращает ResponseEntity со статусом NOT_FOUND и сообщением об ошибке.
     */
    @ExceptionHandler(NoAccessException.class)
    public ResponseEntity<BaseWebResponse> handleNoAccessException(@NonNull final NoAccessException exc) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseWebResponse(createErrorMessage(exc)));
    }

    /**
     * Формирует сообщение об исключении для отправки клиенту.
     *
     * @param exception исключение.
     * @return Возвращает информационное сообщение об исключении.
     */
    private String createErrorMessage(Exception exception) {
        final String message = exception.getMessage();
        log.error(ExceptionHandlerUtils.buildErrorMessage(exception));
        return message;
    }
}
