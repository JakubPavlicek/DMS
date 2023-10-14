package com.dms.service;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.SortFieldItem;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.exception.RevisionNotFoundException;
import com.dms.repository.DocumentRevisionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    private void replaceDocumentWithAdjacentRevision(Document document, Long currentRevisionId) {
        DocumentRevision currentDocumentRevision = documentCommonService.getRevision(currentRevisionId);

        Long currentVersion = currentDocumentRevision.getVersion();
        DocumentRevision replacingRevision = revisionRepository.findPreviousByDocumentAndVersion(document, currentVersion)
                                                               .orElse(revisionRepository.findNextByDocumentAndVersion(document, currentVersion)
                                                                                         .orElse(null));
        if (replacingRevision == null)
            throw new RevisionNotFoundException("Nebyla nalezena nahrazujici revize pro revizi s ID: " + currentRevisionId);

        documentCommonService.updateDocumentToRevision(document, replacingRevision);
    }

    private boolean isRevisionSetAsCurrent(Document document, Long revisionId) {
        DocumentRevision documentRevision = documentCommonService.getRevision(revisionId);

        return document.getHash()
                       .equals(documentRevision.getHash());
    }

    @Transactional
    public void deleteRevision(Long revisionId) {
        DocumentRevision documentRevision = documentCommonService.getRevision(revisionId);
        Document document = documentRevision.getDocument();

        if (isRevisionSetAsCurrent(document, revisionId))
            replaceDocumentWithAdjacentRevision(document, revisionId);

        documentCommonService.deleteBlobIfDuplicateHashNotExists(documentRevision.getHash());
        revisionRepository.deleteByRevisionId(revisionId);

        updateRevisionVersionsForDocument(document);
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

    public Page<DocumentRevisionDTO> getRevisionsWithPagingAndSorting(int pageNumber, int pageSize, List<SortFieldItem> sortFieldItems) {
        Pageable pageable = documentCommonService.createPageable(pageNumber, pageSize, sortFieldItems);

        Page<DocumentRevision> revisions = revisionRepository.findAll(pageable);
        List<DocumentRevisionDTO> revisionDTOs = revisions.stream()
                                                          .map(documentCommonService::mapRevisionToRevisionDto)
                                                          .toList();

        return new PageImpl<>(revisionDTOs, pageable, revisions.getTotalElements());
    }

}
