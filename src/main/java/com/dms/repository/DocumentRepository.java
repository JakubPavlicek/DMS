package com.dms.repository;

import com.dms.entity.Document;
import com.dms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Optional<Document> findByDocumentId(String documentId);

    List<Document> findAllByAuthor(User author);

    boolean existsByDocumentId(String documentId);

    @Query("SELECT COUNT(document.hash) > 1 FROM Document document WHERE document.hash = :hash")
    boolean duplicateHashExists(String hash);

    @Query("SELECT document.createdAt FROM Document document WHERE document.documentId = :documentId")
    Optional<LocalDateTime> getCreatedAtByDocumentId(String documentId);

    @Query("SELECT COUNT(document.path) >= 1 FROM Document document WHERE document.path = :path AND document.name = :filename AND document.author = :user")
    boolean pathWithFileAlreadyExists(String path, String filename, User user);

}
