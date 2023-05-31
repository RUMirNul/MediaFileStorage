package edu.rumirnul.mediafilestorage.service;

import edu.rumirnul.mediafilestorage.dto.FileDto;

import java.io.InputStream;

public interface S3Service {
    void saveFile(FileDto fileDto);
    InputStream getFileByName(String fileName);
    void deleteFileByName(String fileName);
}
