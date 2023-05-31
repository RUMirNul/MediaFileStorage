package edu.rumirnul.mediafilestorage.service.impl;

import edu.rumirnul.mediafilestorage.entity.FileData;
import edu.rumirnul.mediafilestorage.repository.FileDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * Класс сервис работающий с данными о файлах.
 *
 * @author Alexey Svistunov
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileDataService {

    private final FileDataRepository fileDataRepository;

    /**
     * Сохранение метаинформации о файле.
     *
     * @param entity сущность для сохранения.
     * @return Возвращает сохранённую сущность.
     */
    public FileData save(FileData entity) {
        log.info("Saving an entity: {}", entity);

        FileData savedEntity = fileDataRepository.save(entity);
        log.info("Saved entity: {}", savedEntity);

        return savedEntity;
    }

    /**
     * Получение метаинформации о файле по id.
     *
     * @param id уникальный идентификатор.
     * @return Возвращает найденную сущность.
     */
    public FileData getById(Long id) {
        log.info("Getting an entity with id: {}", id);

        FileData result = fileDataRepository.getReferenceById(id);
        log.info("Received entity: {}", result);

        return result;
    }

    /**
     * Удаление метаинформации из БД.
     *
     * @param entity сущность, которую нужно удалить.
     */
    public void delete(FileData entity) {
        log.info("Deleting an entity: {}", entity);

        fileDataRepository.delete(entity);
    }

    /**
     * Проверяет по id, что метаинформация о файле есть в БД.
     *
     * @param id уникальный идентификатор.
     * @return Возвращает true - сущность с таким id есть в БД, false - иначе
     */
    public boolean existsById(Long id) {
        log.info("Checking for the existence of an entity with id: {}", id);

        return fileDataRepository.existsById(id);
    }

    /**
     * Возвращает метаинформацию о файле по имени файла.
     *
     * @param name имя файла.
     * @return Возвращает найденную сущность.
     */
    public FileData getByName(String name) {
        log.info("Getting an entity with name: {}", name);

        FileData result = fileDataRepository.findFileDataByFileName(name);
        log.info("Received entity: {}", result);

        return result;
    }

}
