package com.dms.repository;

import com.dms.entity.DocumentRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRevisionRepository extends JpaRepository<DocumentRevision, String> {
    Optional<DocumentRevision> findByDocument_DocumentIdAndRevisionId(String documentId, Long revisionId);
}
