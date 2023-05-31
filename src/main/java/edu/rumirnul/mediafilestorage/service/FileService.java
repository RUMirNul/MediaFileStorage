package edu.rumirnul.mediafilestorage.service;

import edu.rumirnul.mediafilestorage.entity.FileData;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {
    FileData saveFile(MultipartFile multipartFile);
    InputStream getFileById(Long fileId);
    FileData getFileMetadata(Long fileId);
    void deleteFileById(Long fileId);
}

