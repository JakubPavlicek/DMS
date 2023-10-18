package com.dms.service;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.FilterItem;
import com.dms.dto.SortItem;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.exception.RevisionNotFoundException;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.specification.DocumentFilterSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
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

    public Page<DocumentRevisionDTO> getRevisions(int pageNumber, int pageSize, String sort, String filter) {
        List<SortItem> sortItems = documentCommonService.parseSortItems(sort);
        List<FilterItem> filterItems = documentCommonService.parseFilterItems(filter);

        Pageable pageable = documentCommonService.createPageable(pageNumber, pageSize, sortItems);

        Page<DocumentRevision> revisions = getFilteredRevisions(filterItems, pageable);
        List<DocumentRevisionDTO> revisionDTOs = revisions.stream()
                                                          .map(documentCommonService::mapRevisionToRevisionDto)
                                                          .toList();

        return new PageImpl<>(revisionDTOs, pageable, revisions.getTotalElements());
    }

    private Page<DocumentRevision> getFilteredRevisions(List<FilterItem> filterItems, Pageable pageable) {
        if (Objects.isNull(filterItems))
            return revisionRepository.findAll(pageable);

        Specification<DocumentRevision> specification = DocumentFilterSpecification.filterByItems(filterItems);
        return revisionRepository.findAll(specification, pageable);
    }

    public DocumentRevisionDTO moveRevision(Long revisionId, String path) {
        DocumentRevision revision = documentCommonService.getRevision(revisionId);

        String filename = revision.getName();

        // user can't have a duplicate path for a document with the same name
        if(revisionRepository.pathWithFileAlreadyExists(path, filename, revision.getAuthor()))
            throw new RuntimeException("Soubor: " + filename + " se jiz v zadane ceste: " + path + " vyskytuje");

        revision.setPath(path);

        DocumentRevision savedRevision = revisionRepository.save(revision);

        return documentCommonService.mapRevisionToRevisionDto(savedRevision);
    }

}
