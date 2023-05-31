package edu.rumirnul.mediafilestorage.service.impl;

import com.google.common.io.Files;
import edu.rumirnul.mediafilestorage.config.FileTypeWhitelistConfiguration;
import edu.rumirnul.mediafilestorage.dto.FileDto;
import edu.rumirnul.mediafilestorage.entity.FileData;
import edu.rumirnul.mediafilestorage.exception.FileNotFoundException;
import edu.rumirnul.mediafilestorage.exception.ForbiddenFileFormatException;
import edu.rumirnul.mediafilestorage.exception.IOAccessException;
import edu.rumirnul.mediafilestorage.exception.NoAccessException;
import edu.rumirnul.mediafilestorage.service.FileService;
import edu.rumirnul.mediafilestorage.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Класс сервис работающий с файлами.
 *
 * @author Alexey Svistunov
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final S3Service s3Service;
    private final FileDataService fileDataService;
    private final FileTypeWhitelistConfiguration fileTypeWhitelistConfiguration;

    /**
     * Сохранение метаданных файла в БД и самого файла в S3 хранилище.
     * Сохраняются только файлы с расширением из белого списка {@link FileTypeWhitelistConfiguration}
     *
     * @param multipartFile файл от клиента.
     * @return Возвращает метаданные файла.
     * @throws ForbiddenFileFormatException в белом списке нет такого расширения файла.
     * @throws IOAccessException            не удалось получить содержимое файла из запроса.
     */
    @Override
    public FileData saveFile(MultipartFile multipartFile) {
        FileData fileData = new FileData();

        String originalFileName = multipartFile.getOriginalFilename() != null
                ? multipartFile.getOriginalFilename() : fileData.getFileName();

        String extension = Files.getFileExtension(originalFileName);
        if (!fileTypeWhitelistConfiguration.getExtensions().contains(StringUtils.lowerCase(extension))) {
            throw new ForbiddenFileFormatException("The file format is not supported");
        }
        String fileName = getFileName(fileData.getFileName(), extension);

        try {
            FileDto fileDto = FileDto.builder()
                    .name(fileName)
                    .inputStream(multipartFile.getInputStream())
                    .size(multipartFile.getInputStream().available())
                    .build();
            s3Service.saveFile(fileDto);
        } catch (IOException e) {
            throw new IOAccessException("Couldn't access the contents of the file");
        }

        fileData.setFileName(fileName);
        fileData.setOriginalName(originalFileName);
        fileData.setExtension(extension);

        fileData = fileDataService.save(fileData);

        log.info("Saved file with file data: {}", fileData);
        return fileData;
    }

    /**
     * Получение файла по уникальному идентификатору.
     *
     * @param fileId уникальный идентификатор файла.
     * @return Возвращает содержимое файла.
     */
    @Override
    @Transactional
    public InputStream getFileById(Long fileId) {
        log.info("Getting a file with id: {}", fileId);

        FileData fileData = getFileMetadata(fileId);
        log.info("File data was received from DB: {}", fileData);

        InputStream inputStream = s3Service.getFileByName(fileData.getFileName());
        log.info("File was received from s3");

        return inputStream;
    }

    /**
     * Получение метаданных файла по уникальному идентификатору.
     *
     * @param fileId уникальный идентификатор файла.
     * @return Возвращает метаданные файла.
     * @throws FileNotFoundException файла нет в хранилище и/или БД.
     */
    @Override
    @Transactional
    public FileData getFileMetadata(Long fileId) {
        log.info("Getting a file data with id: {}", fileId);

        try {
            if (fileDataService.existsById(fileId)) {
                FileData fileData = fileDataService.getById(fileId);
                log.info("File data was received from DB: {}", fileData);

                return fileData;
            } else {
                throw new FileNotFoundException("File with id " + fileId + " not found");
            }
        } catch (Exception e) {
            throw new FileNotFoundException("File with id " + fileId + " not found");
        }
    }

    /**
     * Удаление файла по уникальному идентификатору.
     *
     * @param fileId уникальный идентификатор файла.
     */
    @Override
    @Transactional
    public void deleteFileById(Long fileId) {

        try {
            FileData fileData = getFileMetadata(fileId);

            s3Service.deleteFileByName(fileData.getFileName());
            fileDataService.delete(fileData);
            log.info("Deleted file with id: {}", fileId);
        } catch (NoAccessException ex) {
            throw ex;
        } catch (Exception ignore) {
            log.error("File with id {} not found", fileId);
        }
    }

    /**
     * Получение полного имени файла по имени файла и расширению файла.
     *
     * @param fileName  имя файла.
     * @param extension расширение файла.
     * @return Возвращает полное имя файла.
     */
    private String getFileName(String fileName, String extension) {
        if (!extension.equals("")) {
            fileName = fileName + "." + extension;
        }
        return fileName;
    }
}
