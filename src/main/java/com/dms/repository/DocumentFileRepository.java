package com.dms.repository;

import com.dms.entity.DocumentFile;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFile, String>, RevisionRepository<DocumentFile, String, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE DocumentFile file SET file.filePath = :path WHERE file.fileId = :id")
    void updateDocumentFileLocationById(String id, String path);
}
