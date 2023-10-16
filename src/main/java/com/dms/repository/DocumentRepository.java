package com.dms.repository;

import com.dms.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String>, JpaSpecificationExecutor<Document> {

    @Query("SELECT COUNT(document.hash) > 1 FROM Document document WHERE document.hash = :hash")
    boolean duplicateHashExists(String hash);

    @Query("SELECT document.createdAt FROM Document document WHERE document.documentId = :documentId")
    Optional<LocalDateTime> getCreatedAtByDocumentId(String documentId);

}
