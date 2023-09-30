package com.dms.repository;

import com.dms.entity.DocumentRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRevisionRepository extends JpaRepository<DocumentRevision, String> {
    Optional<DocumentRevision> findByFileIdAndRevisionId(String fileId, Long revisionId);

    List<DocumentRevision> findAllByFileId(String fileId);

    void deleteByFileIdAndRevisionId(String fileId, Long revisionId);

    boolean existsByFileId(String fileId);
}
