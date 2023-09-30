package com.dms.repository;

import com.dms.entity.DocumentRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRevisionRepository extends JpaRepository<DocumentRevision, String> {
    Optional<DocumentRevision> findByDocumentIdAndRevisionId(String documentId, Long revisionId);

    List<DocumentRevision> findAllByDocumentId(String documentId);

    void deleteByDocumentIdAndRevisionId(String documentId, Long revisionId);
}
