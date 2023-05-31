package edu.rumirnul.mediafilestorage.repository;

import edu.rumirnul.mediafilestorage.entity.FileData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDataRepository extends JpaRepository<FileData, Long> {
    FileData findFileDataByFileName(String fileName);
}
