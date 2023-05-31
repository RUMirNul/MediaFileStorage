package edu.rumirnul.mediafilestorage.repository;

import com.vladmihalcea.sql.SQLStatementCountValidator;
import edu.rumirnul.mediafilestorage.config.SystemJpaTest;
import edu.rumirnul.mediafilestorage.entity.FileData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;

import static com.vladmihalcea.sql.SQLStatementCountValidator.*;
import static com.vladmihalcea.sql.SQLStatementCountValidator.assertDeleteCount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Тесты репозитория {@link FileDataRepository}
 */
@SystemJpaTest
class FileDataRepositoryTest {

    @Autowired
    FileDataRepository fileDataRepository;


    // Заранее сохранённая информация в БД. Описана в 2_insert_file_data.sql
    private final static Long DEFAULT_ID = 100L;
    private final static String DEFAULT_FILE_NAME = "testFileName";
    private final static String DEFAULT_ORIGINAL_NAME = "testOriginalName";
    private final static String DEFAULT_EXTENSION = "txt";

    @BeforeEach
    void setUp() {
        SQLStatementCountValidator.reset();
    }


    @DisplayName("Сохранение метаданных о файле. Число insert должно равняться 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void saveFileData_whenCorrectFileData_thenReturnFileDataAndAssertDmlCount() {
        //Given
        String originalFileName = "testFileabcd";
        String fileExtension = "txt";

        FileData fileData = new FileData();
        fileData.setOriginalName(originalFileName);
        fileData.setExtension(fileExtension);

        //When
        FileData savedFile = fileDataRepository.save(fileData);

        //Then
        assertNotNull(savedFile.getId());
        assertNotNull(savedFile.getFileName());
        assertEquals(originalFileName, savedFile.getOriginalName());
        assertEquals(fileExtension, savedFile.getExtension());

        assertSelectCount(0);
        assertInsertCount(1);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Сохранение с FileData = null. Должно выбросить ошибку.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void saveFileData_whenNullArgument_thenThrowsIllegalArgumentException() {
        //When
        Throwable throwable = catchThrowable(() -> fileDataRepository.save(null));

        //Then
        assertThat(throwable).hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Получение метаданных файла по id. Число select должно быть равно 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_file_data.sql"})
    void getFileDataById_whenExistsId_thenReturnFileDataAndAssertDmlCount() {
        //When
        FileData result = fileDataRepository.getReferenceById(DEFAULT_ID);

        //Then
        assertEquals(DEFAULT_ID, result.getId());
        assertEquals(DEFAULT_FILE_NAME, result.getFileName());
        assertEquals(DEFAULT_ORIGINAL_NAME, result.getOriginalName());
        assertEquals(DEFAULT_EXTENSION, result.getExtension());

        assertSelectCount(1);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Получение метаданных файла по id = null. Должно выбросить ошибку.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void getFileDataById_whenNullId_thenThrowsIllegalArgumentException() {
        //When
        Throwable throwable = catchThrowable(() -> fileDataRepository.getReferenceById(null));

        //Then
        assertThat(throwable).hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Получение метаданных файла по id, которого нет в БД. Число всех операций = 0.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_file_data.sql"})
    void getFileDataById_whenNotExistsId_thenAssertDmlCount() {
        //When
        fileDataRepository.getReferenceById(1509L);

        //Then
        assertSelectCount(0);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Удаление метаданных файла по id. Число select должно быть равно 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_file_data.sql"})
    void deleteFileDataById_whenExistsId_thenAssertDmlCount() {
        //When
        fileDataRepository.deleteById(DEFAULT_ID);

        //Then
        assertSelectCount(1);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Удаление метаданных файла по entity FileData. Число select должно быть равно 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_file_data.sql"})
    void deleteFileDataByEntity_whenCorrectEntity_thenAssertDmlCount() {
        //Given
        FileData fileData = FileData.builder()
                .id(DEFAULT_ID)
                .fileName(DEFAULT_FILE_NAME)
                .originalName(DEFAULT_ORIGINAL_NAME)
                .extension(DEFAULT_EXTENSION)
                .build();
        //When
        fileDataRepository.delete(fileData);

        //Then
        assertSelectCount(1);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Удаление метаданных файла по id = null. Должно выбросить ошибку.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void deleteFileDataById_whenNullId_thenThrowsIllegalArgumentException() {
        //When
        Throwable throwable = catchThrowable(() -> fileDataRepository.deleteById(null));

        //Then
        assertThat(throwable).hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Удаление метаданных файла по id, которого нет в БД. Должно выбросить ошибку.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void deleteFileDataById_whenNotExistsId_thenThrowsEmptyResultDataAccessException() {
        //When
        Throwable throwable = catchThrowable(() -> fileDataRepository.deleteById(1509L));

        //Then
        assertThat(throwable).isInstanceOf(EmptyResultDataAccessException.class);
    }

    @DisplayName("Удаление метаданных файла по entity FileData, которого нет в БД. Число всех операций = 0.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql"})
    void deleteFileDataByEntity_whenNotExistsEntity_thenAssertDmlCount() {
        //Given
        FileData fileData = new FileData();

        //When
        fileDataRepository.delete(fileData);

        //Then
        assertSelectCount(0);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Получение метаданных файла по имени файла. Число select должно быть равно 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_file_data.sql"})
    void findFileDataByFileName_whenExistsName_thenReturnFileDataAndAssertDmlCount() {
        //When
        FileData result = fileDataRepository.findFileDataByFileName(DEFAULT_FILE_NAME);

        //Then
        assertEquals(DEFAULT_ID, result.getId());
        assertEquals(DEFAULT_FILE_NAME, result.getFileName());
        assertEquals(DEFAULT_ORIGINAL_NAME, result.getOriginalName());
        assertEquals(DEFAULT_EXTENSION, result.getExtension());

        assertSelectCount(1);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    @DisplayName("Получение метаданных файла по имени файла, которого нет в БД. Число select должно быть равно 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_file_data.sql"})
    void findFileDataByFileName_whenNotExistsFileName_thenAssertDmlCount() {
        //When
        fileDataRepository.findFileDataByFileName("random_name_acde");

        //Then
        assertSelectCount(1);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }


}
