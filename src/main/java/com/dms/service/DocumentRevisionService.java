package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.repository.DocumentRevisionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DocumentRevisionService {

    private final DocumentRevisionRepository revisionRepository;

    private final DocumentServiceCommon documentServiceCommon;
    private final BlobStorageService blobStorageService;

    public List<DocumentRevision> getRevisions() {
        return revisionRepository.findAll();
    }

    public DocumentRevision getRevision(Long revisionId) {
        return revisionRepository.findByRevisionId(revisionId)
                                 .orElseThrow(() -> new RuntimeException("Revize s ID: " + revisionId + " nebyla nalezena"));
    }

    private void replaceDocumentWithAdjacentRevision(String documentId, Long currentRevisionId) {
        Document document = documentServiceCommon.getDocument(documentId);
        DocumentRevision currentDocumentRevision = getRevision(currentRevisionId);

        Long currentVersion = currentDocumentRevision.getVersion();
        DocumentRevision newRevision = revisionRepository.findPreviousByDocumentAndVersion(document, currentVersion)
                                                         .orElse(revisionRepository.findNextByDocumentAndVersion(document, currentVersion)
                                                                                   .orElse(null));
        if (newRevision == null)
            throw new RuntimeException("nebyla nalezena nahrazujici revize pro revizi " + currentRevisionId);

        documentServiceCommon.updateDocumentToRevision(document, newRevision);
    }

    private boolean isRevisionSetAsCurrent(String documentId, Long revisionId) {
        Document document = documentServiceCommon.getDocument(documentId);
        DocumentRevision documentRevision = getRevision(revisionId);

        return document.getHashPointer()
                       .equals(documentRevision.getHashPointer());
    }

    @Transactional
    public String deleteRevision(Long revisionId) {
        DocumentRevision documentRevision = getRevision(revisionId);
        String hash = documentRevision.getHashPointer();

        Document document = documentRevision.getDocument();
        String documentId = document.getDocumentId();

        if (isRevisionSetAsCurrent(documentId, revisionId))
            replaceDocumentWithAdjacentRevision(documentId, revisionId);

        blobStorageService.deleteBlob(hash);
        revisionRepository.deleteByRevisionId(revisionId);

        updateRevisionVersionsForDocument(documentId);

        return "Revision deleted successfully";
    }

    private void updateRevisionVersionsForDocument(String documentId) {
        Document document = documentServiceCommon.getDocument(documentId);
        List<DocumentRevision> documentRevisions = revisionRepository.findAllByDocumentOrderByCreatedAtAsc(document);

        Long version = 1L;
        for (DocumentRevision revision : documentRevisions) {
            revisionRepository.updateVersion(revision, version);
            version++;
        }
    }

    public ResponseEntity<Resource> downloadRevision(Long revisionId) {
        DocumentRevision revision = getRevision(revisionId);
        String hash = revision.getHashPointer();
        byte[] data = blobStorageService.getBlob(hash);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(revision.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + revision.getName() + "\"")
                             .body(new ByteArrayResource(data));
    }

}
