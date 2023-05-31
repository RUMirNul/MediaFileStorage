package edu.rumirnul.mediafilestorage.service.impl;

import com.google.common.io.ByteStreams;
import edu.rumirnul.mediafilestorage.config.MinioConfiguration;
import edu.rumirnul.mediafilestorage.config.MinioContainerConfig;
import edu.rumirnul.mediafilestorage.entity.FileData;
import edu.rumirnul.mediafilestorage.exception.FileNotFoundException;
import edu.rumirnul.mediafilestorage.exception.ForbiddenFileFormatException;
import edu.rumirnul.mediafilestorage.exception.IOAccessException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестирование функционала {@link FileServiceImpl}
 */
@ActiveProfiles("integrationtest")
@SpringBootTest
@ContextConfiguration(classes = {MinioContainerConfig.class}, initializers = {MinioContainerConfig.Initializer.class})
@Transactional
class FileServiceImplTest {

    @Autowired
    FileServiceImpl fileService;
    @Autowired
    MinioClient minioClient;
    @Autowired
    MinioConfiguration minioConfiguration;

    private final static Random RANDOM = new Random();
    private final static String DEFAULT_ORIGINAL_FILE_NAME = "testOriginalFileName.pdf";
    private final static String DEFAULT_FILE_EXTENSION = "pdf";
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
    void saveFile_whenCorrectMultipartFile_thenReturnFileData() {
        //Given
        MultipartFile multipartFile = getCorrectMultipartFile();
        //When

        //Then
        FileData result = fileService.saveFile(multipartFile);

        assertNotNull(result.getId());
        assertNotNull(result.getFileName());
        assertEquals(DEFAULT_ORIGINAL_FILE_NAME, result.getOriginalName());
        assertEquals(DEFAULT_FILE_EXTENSION, result.getExtension());
    }


    @DisplayName("Сохранение файла с ошибкой во время сохранения в minio. Метаданные файла должны быть удалены.")
    @Test
    void saveFile_whenMinioException_thenThrowsFileNotFoundException() throws InterruptedException {
        //Given
        MultipartFile multipartFile = getCorrectMultipartFile();
        String uncorrectedBucketName = "test-bucket-non-exist";
        final String correctBucketName = minioConfiguration.getBucket();
        minioConfiguration.setBucket(uncorrectedBucketName);

        //When
        FileData fileData = fileService.saveFile(multipartFile);
        Thread.sleep(1000);
        long id = fileData.getId();

        Throwable throwable = catchThrowable(() -> fileService.getFileById(id));

        //Then
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);


        //Возвращение корректного имени файла в конфиг.
        minioConfiguration.setBucket(correctBucketName);
    }

    @DisplayName("Сохранение файла с InputStream = null. Должно выбросить ошибку NullPointerException.")
    @Test
    void saveFile_whenNullInputStream_thenThrowsNullPointerException() {
        //Given
        MultipartFile multipartFile = new MultipartFile() {
            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getOriginalFilename() {
                return DEFAULT_ORIGINAL_FILE_NAME;
            }

            @Override
            public String getContentType() {
                return "";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @SneakyThrows
            @Override
            public long getSize() {
                return 100L;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return new byte[]{};
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {

            }
        };

        //When
        Throwable throwable = catchThrowable(() -> fileService.saveFile(multipartFile));

        //Then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Сохранение файла с некорректным InputStream. Должно выбросить ошибку IOAccessException.")
    @Test
    void saveFile_whenInputStreamThrowIOException_thenThrowsIOAccessException() {
        //Given
        MultipartFile multipartFile = new MultipartFile() {
            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getOriginalFilename() {
                return DEFAULT_ORIGINAL_FILE_NAME;
            }

            @Override
            public String getContentType() {
                return "";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @SneakyThrows
            @Override
            public long getSize() {
                return 100L;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return new byte[]{};
            }

            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException();
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {

            }
        };

        //When
        Throwable throwable = catchThrowable(() -> fileService.saveFile(multipartFile));

        //Then
        assertThat(throwable).isInstanceOf(IOAccessException.class);
    }

    @DisplayName("Сохранение файла с не разрешённым расширением файла. Должно выбросить ошибку ForbiddenFileFormatException.")
    @Test
    void saveFile_whenUnresolvedFileExtension_thenThrowsForbiddenFileFormatException() {
        //Given
        MultipartFile multipartFile = new MultipartFile() {
            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getOriginalFilename() {
                return "testOriginalFileName.pdfabcde";
            }

            @Override
            public String getContentType() {
                return "";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @SneakyThrows
            @Override
            public long getSize() {
                return 100L;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return new byte[]{10, 11};
            }

            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException();
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {

            }
        };

        //When
        Throwable throwable = catchThrowable(() -> fileService.saveFile(multipartFile));

        //Then
        assertThat(throwable).isInstanceOf(ForbiddenFileFormatException.class);

    }

    @DisplayName("Сохранение файла с MultipartFile = null. Должно выбросить ошибку NullPointerException.")
    @Test
    void saveFile_whenNullMultipartFile_thenThrowsNullPointerException() {
        //Given
        MultipartFile multipartFile = null;

        //When
        Throwable throwable = catchThrowable(() -> fileService.saveFile(multipartFile));

        //Then
        assertThat(throwable).isInstanceOf(NullPointerException.class);

    }


    @DisplayName("Получение файла по id. Должно пройти успешно.")
    @Test
    void getFileById_whenExistsId_thenReturnInputStreamWithFileData() throws IOException, InterruptedException {
        //Given
        MultipartFile multipartFile = getCorrectMultipartFile();

        //When
        FileData fileData = fileService.saveFile(multipartFile);
        //Ожидание, пока завершиться асинхронный метод сохранения файла.
        Thread.sleep(1000);

        //Then
        InputStream result = fileService.getFileById(fileData.getId());
        assertArrayEquals(ByteStreams.toByteArray(multipartFile.getInputStream()), ByteStreams.toByteArray(result));
    }

    @DisplayName("Получение файла по id, которого нет в БД. Должно пройти успешно.")
    @Test
    void getFileById_whenNonExistsId_thenThrowsFileNotFoundException() {
        //Given
        long id = RANDOM.nextLong();

        //When
        Throwable throwable = catchThrowable(() -> fileService.getFileById(id));

        //Then
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @DisplayName("Получение файла по id = null. Должно пройти успешно.")
    @Test
    void getFileById_whenNullId_thenThrowsFileNotFoundException() {
        //Given
        Long id = null;
        //When
        Throwable throwable = catchThrowable(() -> fileService.getFileById(id));

        //Then
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }


    @DisplayName("Получение метаинформации о файле. Должно пройти успешно.")
    @Test
    void getFileMetadata_whenExistsId_thenReturnFileData() throws InterruptedException {
        //Given
        MultipartFile multipartFile = getCorrectMultipartFile();

        //When
        FileData fileData = fileService.saveFile(multipartFile);
        //Ожидание, пока завершиться асинхронный метод сохранения файла.
        Thread.sleep(1000);
        long id = fileData.getId();

        //Then
        FileData result = fileService.getFileMetadata(id);

        assertEquals(fileData.getId(), result.getId());
        assertEquals(fileData.getFileName(), result.getFileName());
        assertEquals(fileData.getOriginalName(), result.getOriginalName());
        assertEquals(fileData.getExtension(), result.getExtension());
    }

    @DisplayName("Получение метаинформации о файле по id, которого нет в БД. Должно выбросить ошибку FileNotFoundException.")
    @Test
    void getFileMetadata_whenNotExistsId_thenThrowsFileNotFoundException() {
        //Given
        long id = 1509;

        //When
        Throwable throwable = catchThrowable(() -> fileService.getFileMetadata(id));

        //Then
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @DisplayName("Получение метаинформации о файле по id = null. Должно выбросить ошибку FileNotFoundException.")
    @Test
    void getFileMetadata_whenNullId_thenThrowsFileNotFoundException() {
        //Given
        Long id = null;

        //When
        Throwable throwable = catchThrowable(() -> fileService.getFileMetadata(id));

        //Then
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @DisplayName("Удаление файла по id. Должно пройти успешно.")
    @Test
    void deleteFileById_whenExistsId_thenCorrect() throws InterruptedException {
        //Given
        MultipartFile multipartFile = getCorrectMultipartFile();


        //When
        FileData fileData = fileService.saveFile(multipartFile);

        //Ожидание, пока завершиться асинхронный метод сохранения файла.
        Thread.sleep(1000);
        long id = fileData.getId();

        //Then
        fileService.deleteFileById(id);
        //Проверка, что файл был удалён.
        Throwable throwable = catchThrowable(() -> fileService.getFileMetadata(id));
        assertThat(throwable).isInstanceOf(FileNotFoundException.class);
    }

    @DisplayName("Удаление файла по id, которого нет в бд. Должно пройти успешно.")
    @Test
    void deleteFileById_whenNotExistsId_thenCorrect() {
        //Given
        long id = RANDOM.nextLong();

        //When

        //Then
        fileService.deleteFileById(id);
    }

    @DisplayName("Удаление файла по id = null. Должно пройти успешно.")
    @Test
    void deleteFileById_whenNullId_thenCorrect() {
        //Given
        Long id = null;

        //When

        //Then
        fileService.deleteFileById(id);
    }



    private static MultipartFile getCorrectMultipartFile() {

        return new MultipartFile() {
            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getOriginalFilename() {
                return DEFAULT_ORIGINAL_FILE_NAME;
            }

            @Override
            public String getContentType() {
                return "";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @SneakyThrows
            @Override
            public long getSize() {
                return getInputStream().available();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return ByteStreams.toByteArray(getInputStream());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return getDefaultInputStream();
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {

            }
        };
    }

    private static InputStream getDefaultInputStream() {
        return new ByteArrayInputStream(BYTE_DATA);
    }
}
