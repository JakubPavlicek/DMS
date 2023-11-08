package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.exception.RevisionDeletionException;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class DeletionService {

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository revisionRepository;

    private final ManagementService managementService;

    @Transactional
    public void deleteDocumentWithRevisions(String documentId) {
        log.debug("Request - Deleting document with revisions: documentId={}", documentId);

        Document document = managementService.getDocument(documentId);

        List<DocumentRevision> documentRevisions = document.getRevisions();
        documentRevisions.forEach(revision -> managementService.deleteBlobIfDuplicateHashNotExists(revision.getHash()));

        documentRepository.delete(document);

        log.info("Document {} with revisions deleted successfully", documentId);
    }

    private void replaceDocumentWithAdjacentRevision(Document document) {
        Long currentVersion = document.getVersion();
        DocumentRevision replacingRevision = revisionRepository.findPreviousByDocumentAndVersion(document, currentVersion)
                                                               .orElse(revisionRepository.findNextByDocumentAndVersion(document, currentVersion)
                                                                                         .orElse(null));

        log.debug("Replacing document with adjacent revision: document={}, replacingRevision={}", document, replacingRevision);

        if (replacingRevision == null) {
            throw new RevisionDeletionException(
                "Revision cannnot be deleted because this is the only version left for the document with ID: " + document.getDocumentId()
            );
        }

        managementService.updateDocumentToRevision(document, replacingRevision);
    }

    private boolean isRevisionSetAsCurrent(DocumentRevision revision, Document document) {
        return revision.getVersion().equals(document.getVersion());
    }

    @Transactional
    public void deleteRevision(String revisionId) {
        log.debug("Request - Deleting revision: revisionId={}", revisionId);

        DocumentRevision revision = managementService.getRevision(revisionId);
        Document document = revision.getDocument();

        // revision which is also a current document is being deleted -> switch document to adjacent revision
        if (isRevisionSetAsCurrent(revision, document)) {
            replaceDocumentWithAdjacentRevision(document);
        }

        managementService.deleteBlobIfDuplicateHashNotExists(revision.getHash());
        revisionRepository.deleteByRevisionId(revisionId);

        updateRevisionVersionsForDocument(document);

        // document's previous version was deleted -> decrement current document's version
        if (revision.getVersion().compareTo(document.getVersion()) < 0) {
            document.setVersion(document.getVersion() - 1);
        }

        log.info("Revision {} deleted successfully", revisionId);
    }

    public void updateRevisionVersionsForDocument(Document document) {
        log.debug("Updating revision versions for document: documentId={}", document.getDocumentId());

        List<DocumentRevision> documentRevisions = revisionRepository.findAllByDocumentOrderByCreatedAtAsc(document);

        Long version = 1L;
        for (DocumentRevision revision : documentRevisions) {
            revisionRepository.updateVersion(revision, version);
            version++;
        }
    }

}
