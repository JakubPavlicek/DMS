package com.dms.repository;

import com.dms.entity.DocumentFileRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentFileRevisionRepository extends JpaRepository<DocumentFileRevision, String> {
    Optional<DocumentFileRevision> findByFileIdAndRevisionId(String fileId, Long revisionId);

    List<DocumentFileRevision> findAllByFileId(String fileId);

    void deleteByFileIdAndRevisionId(String fileId, Long revisionId);

    boolean existsByFileId(String fileId);
}
