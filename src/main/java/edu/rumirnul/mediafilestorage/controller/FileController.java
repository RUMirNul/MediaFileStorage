package edu.rumirnul.mediafilestorage.controller;

import com.google.common.io.ByteStreams;
import edu.rumirnul.mediafilestorage.constant.WebConstant;
import edu.rumirnul.mediafilestorage.entity.FileData;
import edu.rumirnul.mediafilestorage.response.FileDataResponse;
import edu.rumirnul.mediafilestorage.response.FileUploadResponse;
import edu.rumirnul.mediafilestorage.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping(value = WebConstant.VERSION_URL + "/file")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/create", consumes = {MULTIPART_FORM_DATA_VALUE}, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @Operation(summary = "Save file and meta information.")
    public ResponseEntity<FileUploadResponse> fileUpload(@RequestParam("file") MultipartFile multipartFile) {

        final FileData fileItem = fileService.saveFile(multipartFile);
        final FileUploadResponse response = new FileUploadResponse(fileItem.getId());

        log.info("Response with file id: {}", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(path = "/get/{id}")
    @Operation(summary = "Get file by file id.")
    public void fileDownload(@PathVariable("id") Long fileId, HttpServletResponse response) throws IOException {
        log.info("Request to receive a file with id: {}", fileId);

        InputStream inputStream = fileService.getFileById(fileId);

        FileData fileData = fileService.getFileMetadata(fileId);
        String encodedOriginalName = URLEncoder.encode(fileData.getOriginalName(), String.valueOf(StandardCharsets.UTF_8));
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedOriginalName);
        response.setCharacterEncoding("UTF-8");

        ByteStreams.copy(inputStream, response.getOutputStream());
        response.flushBuffer();

        log.info("File was sent in response");
    }

    @GetMapping(path = "/data/get/{id}")
    @Operation(summary = "Get file data by file id")
    public ResponseEntity<FileDataResponse> getFileData(@PathVariable("id") Long fileId) {
        log.info("Request to get a file data");

        FileData fileData = fileService.getFileMetadata(fileId);

        FileDataResponse response = new FileDataResponse(fileData.getOriginalName());

        log.info("Response with file data: {}", response);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping(path = "/delete/{id}")
    @Operation(summary = "Delete file by file id.")
    public void fileDelete(@PathVariable("id") Long fileId) {
        log.info("User with id = {}  wants to delete the file with id = {}", "userId", fileId);

        fileService.deleteFileById(fileId);
    }

}