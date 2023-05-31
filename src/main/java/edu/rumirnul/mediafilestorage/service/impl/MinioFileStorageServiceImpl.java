package edu.rumirnul.mediafilestorage.service.impl;

import edu.rumirnul.mediafilestorage.config.MinioConfiguration;
import edu.rumirnul.mediafilestorage.dto.FileDto;
import edu.rumirnul.mediafilestorage.event.FileSaveFailedEvent;
import edu.rumirnul.mediafilestorage.exception.FileNotFoundException;
import edu.rumirnul.mediafilestorage.exception.S3Exception;
import edu.rumirnul.mediafilestorage.service.S3Service;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;

/**
 * Класс сервис работающий с хранилищем файлов minio.
 *
 * @author Alexey Svistunov
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioFileStorageServiceImpl implements S3Service {

    private final MinioClient minioClient;
    private final MinioConfiguration minioConfiguration;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Создание bucket в s3(minio), если его нет во время запуска приложения.
     *
     * @throws RuntimeException ошибка при создании ведра.
     */
    @PostConstruct
    private void createBucketIfNotExists() {
        try {
            String bucketName = minioConfiguration.getBucket();
            if (!minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build())) {

                log.info("No bucket with a name = {}, trying to create it.", bucketName);

                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());

                log.info("Bucket with name: {} was created.", bucketName);
            }
        } catch (Exception e) {
            log.info("Failed when create bucket.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Асинхронное сохранение файла в хранилище.
     *
     * @param fileDto содержимое и информация о сохраняемом файле.
     * @throws S3Exception произошла ошибка при работе с хранилищем.
     */
    @Async
    @Override
    public void saveFile(FileDto fileDto) {
        log.info("Saving a file to s3(minio): {}", fileDto);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfiguration.getBucket())
                    .object(fileDto.getName())
                    .stream(fileDto.getInputStream(), fileDto.getSize(), -1)
                    .build());

            log.info("File has been saved in s3(minio)");
        } catch (Exception e) {
            log.error("Failed to save file in s3(minio)");
            applicationEventPublisher.publishEvent(new FileSaveFailedEvent(fileDto.getName()));
            throw new S3Exception("Failed to save file in s3(minio)");
        }
    }

    /**
     * Получение файла по имени.
     *
     * @param fileName имя файла.
     * @return Возвращает содержимое файла.
     * @throws FileNotFoundException не удалось найти файл в хранилище.
     */
    @Override
    public InputStream getFileByName(String fileName) {
        log.info("Getting from s3(minio) a file with file name: {}", fileName);
        try {
            InputStream result = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfiguration.getBucket())
                    .object(fileName)
                    .build());
            log.info("Received file InputStream: {}", result);
            return result;
        } catch (Exception e) {
            throw new FileNotFoundException("File not found in storage.");
        }
    }

    /**
     * Асинхронное удаление файла из хранилища.
     *
     * @param fileName имя файла.
     */
    @Async
    @Override
    public void deleteFileByName(String fileName) {
        log.info("Deleting from s3(minio) file with file name: {}", fileName);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfiguration.getBucket())
                    .object(fileName)
                    .build());
            log.info("File was deleted from s3(minio). File name: {}", fileName);
        } catch (Exception ignore) {
            log.error("File with name {} not found", fileName);
        }

    }
}
