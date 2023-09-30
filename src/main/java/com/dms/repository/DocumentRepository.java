package com.dms.repository;

import com.dms.entity.Document;
import com.dms.entity.DocumentOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    @Modifying
    @Query("UPDATE Document document SET document.name = :name WHERE document = :document")
    void updateDocumentName(Document document, String name);

    @Modifying
    @Query("UPDATE Document document SET document.path = :path WHERE document = :document")
    void updateDocumentPath(Document document, String path);

    @Modifying
    @Query("UPDATE Document document SET document.extension = :extension WHERE document = :document")
    void updateDocumentExtension(Document document, String extension);

    @Modifying
    @Query("UPDATE Document document SET document.type = :type WHERE document = :document")
    void updateDocumentType(Document document, String type);

    @Modifying
    @Query("UPDATE Document document SET document.author = :author WHERE document = :document")
    void updateDocumentAuthor(Document document, String author);

    @Modifying
    @Query("UPDATE Document document SET document.operation = :operation WHERE document = :document")
    void updateDocumentOperation(Document document, DocumentOperation operation);

    @Modifying
    @Query("UPDATE Document document SET document.data = :data WHERE document = :document")
    void updateDocumentData(Document document, byte[] data);

    @Modifying
    @Query("UPDATE Document document SET document.updatedAt = :updatedAt WHERE document = :document")
    void updateDocumentUpdatedAt(Document document, LocalDateTime updatedAt);
}
