package com.dms.repository;

import com.dms.entity.Document;
import com.dms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Optional<Document> findByDocumentIdAndAuthor(String documentId, User author);

    List<Document> findAllByAuthor(User author);

    Integer countAllByIsArchived(Boolean isArchived);

    @Query("SELECT COUNT(document.hash) > 1 FROM Document document WHERE document.hash = :hash")
    boolean duplicateHashExists(String hash);

    @Query("SELECT COUNT(document.path) >= 1 FROM Document document WHERE document.path = :path AND document.name = :name AND document.author = :author")
    boolean documentWithPathAlreadyExists(String name, String path, User author);

}
