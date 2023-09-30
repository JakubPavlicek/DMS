package com.dms.repository;

import com.dms.entity.DocumentFile;
import com.dms.entity.FileOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFile, String> {
    @Modifying
    @Query("UPDATE DocumentFile file SET file.fileName = :name WHERE file = :file")
    void updateFileName(DocumentFile file, String name);

    @Modifying
    @Query("UPDATE DocumentFile file SET file.filePath = :path WHERE file = :file")
    void updateFilePath(DocumentFile file, String path);

    @Modifying
    @Query("UPDATE DocumentFile file SET file.fileExtension = :extension WHERE file = :file")
    void updateFileExtension(DocumentFile file, String extension);

    @Modifying
    @Query("UPDATE DocumentFile file SET file.fileType = :type WHERE file = :file")
    void updateFileType(DocumentFile file, String type);

    @Modifying
    @Query("UPDATE DocumentFile file SET file.author = :author WHERE file = :file")
    void updateFileAuthor(DocumentFile file, String author);

    @Modifying
    @Query("UPDATE DocumentFile file SET file.fileOperation = :operation WHERE file = :file")
    void updateFileOperation(DocumentFile file, FileOperation operation);

    @Modifying
    @Query("UPDATE DocumentFile file SET file.data = :data WHERE file = :file")
    void updateFileData(DocumentFile file, byte[] data);

    @Modifying
    @Query("UPDATE DocumentFile file SET file.updatedAt = :updatedAt WHERE file = :file")
    void updateFileUpdatedAt(DocumentFile file, LocalDateTime updatedAt);
}
