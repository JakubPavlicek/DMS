package com.dms.repository;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRevisionRepository extends JpaRepository<DocumentRevision, Long>, JpaSpecificationExecutor<DocumentRevision> {

    Optional<DocumentRevision> findByRevisionIdAndAuthor(String revisionId, User author);

    Optional<DocumentRevision> findByDocumentAndRevisionId(Document document, String revisionId);

    List<DocumentRevision> findAllByDocumentOrderByCreatedAtAsc(Document document);

    void deleteByRevisionId(String revisionId);

    @Query("SELECT COUNT(revision.hash) > 1 FROM DocumentRevision revision WHERE revision.hash = :hash")
    boolean duplicateHashExists(String hash);

    @Query("SELECT revision FROM DocumentRevision revision WHERE revision.document = :document AND revision.version < :version ORDER BY revision.version DESC LIMIT 1")
    Optional<DocumentRevision> findPreviousByDocumentAndVersion(Document document, Long version);

    @Query("SELECT revision FROM DocumentRevision revision WHERE revision.document = :document AND revision.version > :version ORDER BY revision.version ASC LIMIT 1")
    Optional<DocumentRevision> findNextByDocumentAndVersion(Document document, Long version);

    @Query("SELECT MAX(revision.version) FROM DocumentRevision revision WHERE revision.document = :document")
    Optional<Long> findLastRevisionVersionByDocument(Document document);

    @Modifying
    @Query("UPDATE DocumentRevision revision SET revision.version = :version WHERE revision = :documentRevision")
    void updateVersion(DocumentRevision documentRevision, Long version);

}
