package edu.rumirnul.mediafilestorage.controller;

import com.google.common.io.ByteStreams;
import edu.rumirnul.mediafilestorage.config.MinioContainerConfig;
import edu.rumirnul.mediafilestorage.constant.WebConstant;
import edu.rumirnul.mediafilestorage.entity.FileData;
import edu.rumirnul.mediafilestorage.exception.FileNotFoundException;
import edu.rumirnul.mediafilestorage.exception.ForbiddenFileFormatException;
import edu.rumirnul.mediafilestorage.repository.FileDataRepository;
import edu.rumirnul.mediafilestorage.service.impl.FileServiceImpl;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты контроллера {@link FileController}
 */

@ActiveProfiles("integrationtest")
@SpringBootTest
@ContextConfiguration(classes = {MinioContainerConfig.class}, initializers = {MinioContainerConfig.Initializer.class})
@AutoConfigureMockMvc
class FileControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    FileDataRepository fileDataRepository;
    @Autowired
    FileServiceImpl fileService;

    private static final Random RANDOM = new Random();
    private static final String baseUrl = WebConstant.VERSION_URL + "/file";
    private final static String DEFAULT_ORIGINAL_FILE_NAME = "testOriginalFileName.pdf";
    private final static byte[] BYTE_DATA = new byte[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5};


    @BeforeEach
    public void resetDB() {
        fileDataRepository.deleteAll();
    }


    @DisplayName("Сохранение файла. Должно пройти успешно.")
    @Test
    void fileUpload_whenCorrectRequest_thenStatus201AndReturnFileUploadResponse() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "cartina.pdf",
                "text/plain", "test data".getBytes());

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(baseUrl + "/create")
                        .file(mockMultipartFile))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @DisplayName("Сохранение файла с неразрешенным расширением файла. Должно вернуть код 415 и сообщение об ошибке.")
    @Test
    void fileUpload_whenUnauthorizedFileExtension_thenStatus415AndExceptionMessage() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "cartina.pdfabcdef",
                "text/plain", "test data".getBytes());

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(baseUrl + "/create")
                        .file(mockMultipartFile))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(mvcResult -> mvcResult.getResolvedException()
                        .getClass()
                        .equals(ForbiddenFileFormatException.class));
    }

    @DisplayName("Получение файла. Должно пройти успешно.")
    @Test
    void fileDownload_whenDownload_thenStatus200AndReturnContentAsByteArray() throws Exception {
        MultipartFile multipartFile = getCorrectMultipartFile();

        FileData fileData = fileService.saveFile(multipartFile);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(baseUrl + "/get/" + fileData.getId()))
                .andExpect(status().isOk())
                .andReturn();

        assertArrayEquals(ByteStreams.toByteArray(multipartFile.getInputStream()), result.getResponse().getContentAsByteArray());
    }

    @DisplayName("Получение файла, которого нет. Должно вернуть код 404 и сообщение об ошибке.")
    @Test
    void fileDownload_whenFileNotExist_thenStatus404AndExceptionMessage() throws Exception {
        long fileId = RANDOM.nextLong();

        mockMvc.perform(MockMvcRequestBuilders
                        .get(baseUrl + "/get/" + fileId))
                .andExpect(status().isNotFound())
                .andExpect(mvcResult -> mvcResult.getResolvedException()
                        .getClass()
                        .equals(FileNotFoundException.class));
    }

    @DisplayName("Получение метаинформации о файле. Должно пройти успешно.")
    @Test
    void getFileData_whenGetData_thenStatus200AndReturnFileDataResponse() throws Exception {
        MultipartFile multipartFile = getCorrectMultipartFile();

        FileData fileData = fileService.saveFile(multipartFile);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(baseUrl + "/data/get/" + fileData.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalFileName").value(fileData.getOriginalName()));
    }

    @DisplayName("Получение метаинформации о файле, которого нет. Должно вернуть код 404 и сообщение об ошибке.")
    @Test
    void getFileData_whenFileDataNotExist_thenStatus404AndExceptionMessage() throws Exception {
        long fileId = RANDOM.nextLong();

        mockMvc.perform(MockMvcRequestBuilders
                        .get(baseUrl + "/data/get/" + fileId))
                .andExpect(status().isNotFound())
                .andExpect(mvcResult -> mvcResult.getResolvedException()
                        .getClass()
                        .equals(FileNotFoundException.class));
    }

    @DisplayName("Удаление файла. Должно пройти успешно.")
    @Test
    void fileDelete_whenDelete_thenReturnStatus200() throws Exception {
        MultipartFile multipartFile = getCorrectMultipartFile();
        String userId = "user";

        FileData fileData = fileService.saveFile(multipartFile);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete(baseUrl + "/delete/" + fileData.getId()))
                .andExpect(status().isOk());
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
