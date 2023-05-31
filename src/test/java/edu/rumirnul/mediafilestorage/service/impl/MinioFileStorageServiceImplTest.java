package edu.rumirnul.mediafilestorage.service.impl;

import com.google.common.io.ByteStreams;
import edu.rumirnul.mediafilestorage.config.MinioConfiguration;
import edu.rumirnul.mediafilestorage.config.MinioContainerConfig;
import edu.rumirnul.mediafilestorage.dto.FileDto;
import edu.rumirnul.mediafilestorage.entity.FileData;
import edu.rumirnul.mediafilestorage.event.FileSaveFailedEvent;
import edu.rumirnul.mediafilestorage.exception.FileNotFoundException;
import edu.rumirnul.mediafilestorage.exception.S3Exception;
import edu.rumirnul.mediafilestorage.service.S3Service;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Тестирование функционала {@link MinioFileStorageServiceImpl}
 */
@ActiveProfiles("miniointegrationtest")
@SpringBootTest
@ContextConfiguration(classes = {MinioContainerConfig.class}, initializers = {MinioContainerConfig.Initializer.class})
@RecordApplicationEvents
class MinioFileStorageServiceImplTest {

    @Autowired
    S3Service s3Service;
    @Autowired
    MinioClient minioClient;
    @Autowired
    MinioConfiguration minioConfiguration;
    @Autowired
    ApplicationEvents applicationEvents;
    @MockBean
    FileDataService fileDataService;

    private final static byte[] BYTE_DATA = new byte[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5};


    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, ServerException, InsufficientDataException, ErrorResponseException, IOException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (!minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioConfiguration.getBucket())
                .build())) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioConfiguration.getBucket())
                    .build());
        }
    }

    @DisplayName("Сохранение файла. Должно пройти успешно.")
    @Test
    void saveFile_whenCorrectFileDto_thenCorrectSave() throws IOException {
        //Given
        FileDto fileDto = createCorrectFileDto();

        //When
        s3Service.saveFile(fileDto);

        //Then
        InputStream result = s3Service.getFileByName(fileDto.getName());
        assertArrayEquals(ByteStreams.toByteArray(getDefaultInputStream()), ByteStreams.toByteArray(result));
    }

    @DisplayName("Сохранение файла в bucket, которого нет. Должно отправить event FileSaveFailedEvent и выбросить ошибку S3Exception.")
    @Test
    void saveFile_whenNotExistsBucket_thenThrowsS3Exception() throws IOException {
        //Given
        FileDto fileDto = createCorrectFileDto();
        String uncorrectedBucketName = "test-bucket-non-exist";
        final String correctBucketName = minioConfiguration.getBucket();
        minioConfiguration.setBucket(uncorrectedBucketName);

        FileData fileData = new FileData();
        fileData.setFileName(fileDto.getName());
        fileData.setId(10L);

        //When
        when(fileDataService.getByName(fileDto.getName())).thenReturn(fileData);
        doNothing().when(fileDataService).delete(fileData);

        Throwable throwable = catchThrowable(() -> s3Service.saveFile(fileDto));

        //Then
        assertEquals(1, applicationEvents
                .stream(FileSaveFailedEvent.class)
                .filter(event -> event.getName().equals(fileDto.getName()))
                .count());

        assertThat(throwable).isInstanceOf(S3Exception.class);
        //Возвращение корректного имени файла в конфиг.
        minioConfiguration.setBucket(correctBucketName);
    }

    @DisplayName("Сохранение файла с name = null. Должно отправить event FileSaveFailedEvent и выбросить ошибку S3Exception.")
    @Test
    void saveFile_whenNullFileName_thenThrowsS3Exception() throws IOException {
        //Given
        FileDto fileDto = createCorrectFileDto();
        fileDto.setName(null);

        FileData fileData = new FileData();
        fileData.setFileName(fileDto.getName());
        fileData.setId(10L);

        //When
        when(fileDataService.getByName(fileDto.getName())).thenReturn(fileData);
        doNothing().when(fileDataService).delete(fileData);

        Throwable throwable = catchThrowable(() -> s3Service.saveFile(fileDto));

        //Then
        assertEquals(1, applicationEvents
                .stream(FileSaveFailedEvent.class)
                .count());

        assertThat(throwable).isInstanceOf(S3Exception.class);
    }

    @DisplayName("Сохранение файла неверным полем size. Должно отправить event FileSaveFailedEvent и выбросить ошибку S3Exception.")
    @Test
    void saveFile_whenUncorrectedSize_thenThrowsS3Exception() throws IOException {
        //Given
        FileDto fileDto = createCorrectFileDto();
        fileDto.setSize(1509000L);

        FileData fileData = new FileData();
        fileData.setFileName(fileDto.getName());
        fileData.setId(10L);

        //When
        when(fileDataService.getByName(fileDto.getName())).thenReturn(fileData);
        doNothing().when(fileDataService).delete(fileData);

        Throwable throwable = catchThrowable(() -> s3Service.saveFile(fileDto));

        //Then
        assertEquals(1, applicationEvents
                .stream(FileSaveFailedEvent.class)
                .count());

        assertThat(throwable).isInstanceOf(S3Exception.class);
    }

    @DisplayName("Получение файла по имени. Должно пройти успешно.")
    @Test
    void getFileByName_whenExistsFileName_thenReturnInputStream() throws IOException {
        //Given
        FileDto fileDto = createCorrectFileDto();

        //When
        s3Service.saveFile(fileDto);

        //Then
        InputStream result = s3Service.getFileByName(fileDto.getName());
        assertArrayEquals(ByteStreams.toByteArray(getDefaultInputStream()), ByteStreams.toByteArray(result));
    }

    @DisplayName("Получение файла, которого нет в хранилище. Должно выбросить ошибку FileNotFoundException.")
    @Test
    void getFileByName_whenNotExistsFileName_thenThrowsFileNotFoundException() {
        //Given
        String fileName = UUID.randomUUID().toString();

        //When

        //Then
        Throwable throwable = catchThrowable(() -> s3Service.getFileByName(fileName));
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @DisplayName("Получение файла с name = null. Должно выбросить ошибку FileNotFoundException.")
    @Test
    void getFileByName_whenNullFileName_thenThrowsFileNotFoundException() {
        //Given
        String fileName = null;

        //When

        //Then
        Throwable throwable = catchThrowable(() -> s3Service.getFileByName(fileName));
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @DisplayName("Удаление файла по имени. Должно пройти успешно.")
    @Test
    void deleteFileByName_whenExistsFileName_thenCorrectDelete() throws IOException {
        //Given
        FileDto fileDto = createCorrectFileDto();

        //When
        s3Service.saveFile(fileDto);

        //Then
        s3Service.deleteFileByName(fileDto.getName());

        Throwable throwable = catchThrowable(() -> s3Service.getFileByName(fileDto.getName()));
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @DisplayName("Удаление файла, которого нет в хранилище. Должно пройти успешно.")
    @Test
    void deleteFileByName_whenNotExistsFileName_thenCorrectDelete() {
        //Given
        String fileName = UUID.randomUUID().toString();

        //When

        //Then
        s3Service.deleteFileByName(fileName);
    }

    @DisplayName("Удаление файла c name = null. Должно пройти успешно.")
    @Test
    void deleteFileByName_whenNullFileName_thenCorrectDelete() {
        //Given
        String fileName = null;
        //When

        //Then
        s3Service.deleteFileByName(fileName);
    }

    private FileDto createCorrectFileDto() throws IOException {
        InputStream inputStream = getDefaultInputStream();
        return FileDto.builder()
                .inputStream(inputStream)
                .size(inputStream.available())
                .name(UUID.randomUUID().toString())
                .build();
    }

    private InputStream getDefaultInputStream() {
        return new ByteArrayInputStream(BYTE_DATA);
    }
}
