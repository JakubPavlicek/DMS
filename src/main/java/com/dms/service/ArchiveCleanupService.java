package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.Document_;
import com.dms.repository.DocumentRepository;
import com.dms.specification.DocumentFilterSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class ArchiveCleanupService {

    private final DocumentRepository documentRepository;
    private final DocumentCommonService documentCommonService;

    // run at midnight every day
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupArchive() {
        log.info("Cleaning up the archive");

        Map<String, String> filters = new HashMap<>();
        filters.put(Document_.IS_ARCHIVED, "true");

        Specification<Document> specification = DocumentFilterSpecification.filter(filters);

        Integer archivedDocumentCount = documentRepository.countAllByIsArchived(true);

        int pageSize = 10;
        int pageCount = (int) Math.ceil((double) archivedDocumentCount / pageSize);

        for (int i = 0; i < pageCount; i++) {
            Pageable pageable = PageRequest.of(i, pageSize);
            Page<Document> archivedDocuments = documentRepository.findAll(specification, pageable);

            archivedDocuments.forEach(this::checkDocumentForDeletion);
        }
    }

    private void checkDocumentForDeletion(Document document) {
        if (document.getDeleteAt().isBefore(LocalDateTime.now())) {
            List<DocumentRevision> documentRevisions = document.getRevisions();
            documentRevisions.forEach(revision -> documentCommonService.safelyDeleteBlob(revision.getHash()));

            documentRepository.delete(document);

            log.info("Document {} with revisions deleted successfully from archive", document.getDocumentId());
        }
    }

}
