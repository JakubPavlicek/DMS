package com.dms.service;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.exception.RevisionNotFoundException;
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

    private final DocumentCommonService documentCommonService;

    public DocumentRevisionDTO getRevision(Long revisionId) {
        DocumentRevision revision = documentCommonService.getRevision(revisionId);
        return documentCommonService.mapRevisionToRevisionDto(revision);
    }

    public List<DocumentRevisionDTO> getRevisions() {
        List<DocumentRevision> revisions = revisionRepository.findAll();

        return revisions.stream()
                        .map(documentCommonService::mapRevisionToRevisionDto)
                        .toList();
    }

    private void replaceDocumentWithAdjacentRevision(Document document, Long currentRevisionId) {
        DocumentRevision currentDocumentRevision = documentCommonService.getRevision(currentRevisionId);

        Long currentVersion = currentDocumentRevision.getVersion();
        DocumentRevision newRevision = revisionRepository.findPreviousByDocumentAndVersion(document, currentVersion)
                                                         .orElse(revisionRepository.findNextByDocumentAndVersion(document, currentVersion)
                                                                                   .orElse(null));
        if (newRevision == null)
            throw new RevisionNotFoundException("Nebyla nalezena nahrazujici revize pro revizi s ID: " + currentRevisionId);

        documentCommonService.updateDocumentToRevision(document, newRevision);
    }

    private boolean isRevisionSetAsCurrent(Document document, Long revisionId) {
        DocumentRevision documentRevision = documentCommonService.getRevision(revisionId);

        return document.getHash()
                       .equals(documentRevision.getHash());
    }

    @Transactional
    public String deleteRevision(Long revisionId) {
        DocumentRevision documentRevision = documentCommonService.getRevision(revisionId);

        Document document = documentRevision.getDocument();

        if (isRevisionSetAsCurrent(document, revisionId))
            replaceDocumentWithAdjacentRevision(document, revisionId);

        documentCommonService.deleteBlobIfDuplicateHashNotExists(documentRevision.getHash());
        revisionRepository.deleteByRevisionId(revisionId);

        updateRevisionVersionsForDocument(document);

        return "Revision deleted successfully";
    }

    private void updateRevisionVersionsForDocument(Document document) {
        List<DocumentRevision> documentRevisions = revisionRepository.findAllByDocumentOrderByCreatedAtAsc(document);

        Long version = 1L;
        for (DocumentRevision revision : documentRevisions) {
            revisionRepository.updateVersion(revision, version);
            version++;
        }
    }

    public ResponseEntity<Resource> downloadRevision(Long revisionId) {
        DocumentRevision revision = documentCommonService.getRevision(revisionId);
        String hash = revision.getHash();
        byte[] data = documentCommonService.getBlob(hash);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(revision.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + revision.getName() + "\"")
                             .body(new ByteArrayResource(data));
    }

}
