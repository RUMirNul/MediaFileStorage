package edu.rumirnul.mediafilestorage.listener;

import edu.rumirnul.mediafilestorage.entity.FileData;
import edu.rumirnul.mediafilestorage.event.FileSaveFailedEvent;
import edu.rumirnul.mediafilestorage.service.impl.FileDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Класс Listener для Spring Events.
 * Получает Event и обрабатывает его.
 *
 * @author Alexey Svistunov
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileSaveFailedListener {

    private final FileDataService fileDataService;

    /**
     * Получает Event из Spring Events.
     * Выполняется, когда не удалось сохранить файл в S3 хранилище. Удаляет метаданные файла из БД.
     *
     * @param event содержит информацию о файле с которым произошла ошибка.
     */
    @EventListener(FileSaveFailedEvent.class)
    @Transactional
    public void deleteFileData(FileSaveFailedEvent event) {
        log.info("Deleting file data from DB: {}", event);
        try {
            FileData fileData = fileDataService.getByName(event.getName());
            fileDataService.delete(fileData);
            log.info("File data was deleted from DB: {}", event);
        } catch (Exception ignored) {
            log.error("Failed to delete file data from DB: {}", event);
        }
    }
}