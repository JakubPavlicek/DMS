package com.dms.service;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.filter.FilterItem;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentRevisionService {

    private final DocumentRevisionRepository revisionRepository;

    private final DocumentCommonService documentCommonService;

    public DocumentRevisionDTO getRevision(UUID revisionId) {
        DocumentRevision revision = documentCommonService.getRevision(revisionId);
        return documentCommonService.mapRevisionToRevisionDto(revision);
    }

    private void replaceDocumentWithAdjacentRevision(Document document, UUID currentRevisionId) {
        DocumentRevision currentDocumentRevision = documentCommonService.getRevision(currentRevisionId);

        Long currentVersion = currentDocumentRevision.getVersion();
        DocumentRevision replacingRevision = revisionRepository.findPreviousByDocumentAndVersion(document, currentVersion)
                                                               .orElse(revisionRepository.findNextByDocumentAndVersion(document, currentVersion)
                                                                                         .orElse(null));
        if (replacingRevision == null)
            throw new RevisionNotFoundException("Nebyla nalezena nahrazujici revize pro revizi s ID: " + currentRevisionId);

        documentCommonService.updateDocumentToRevision(document, replacingRevision);
    }

    private boolean isRevisionSetAsCurrent(Document document, UUID revisionId) {
        DocumentRevision documentRevision = documentCommonService.getRevision(revisionId);

        return document.getHash()
                       .equals(documentRevision.getHash());
    }

    @Transactional
    public void deleteRevision(UUID revisionId) {
        DocumentRevision documentRevision = documentCommonService.getRevision(revisionId);
        Document document = documentRevision.getDocument();

        if (isRevisionSetAsCurrent(document, revisionId))
            replaceDocumentWithAdjacentRevision(document, revisionId);

        documentCommonService.deleteBlobIfDuplicateHashNotExists(documentRevision.getHash());
        revisionRepository.deleteByRevisionId(revisionId);

        documentCommonService.updateRevisionVersionsForDocument(document);
    }

    public ResponseEntity<Resource> downloadRevision(UUID revisionId) {
        DocumentRevision revision = documentCommonService.getRevision(revisionId);
        String hash = revision.getHash();
        byte[] data = documentCommonService.getBlob(hash);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(revision.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + revision.getName() + "\"")
                             .body(new ByteArrayResource(data));
    }

    public Page<DocumentRevisionDTO> getRevisions(int pageNumber, int pageSize, String sort, String filter) {
        List<Sort.Order> orders = documentCommonService.getOrdersFromRevisionSort(sort);
        List<FilterItem> filterItems = documentCommonService.getRevisionFilterItemsFromFilter(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(orders));

        Page<DocumentRevision> revisions = getFilteredRevisions(filterItems, pageable);
        List<DocumentRevisionDTO> revisionDTOs = revisions.stream()
                                                          .map(documentCommonService::mapRevisionToRevisionDto)
                                                          .toList();

        return new PageImpl<>(revisionDTOs, pageable, revisions.getTotalElements());
    }

    private Page<DocumentRevision> getFilteredRevisions(List<FilterItem> filterItems, Pageable pageable) {
        Specification<DocumentRevision> specification = DocumentFilterSpecification.filterByItems(filterItems);
        return revisionRepository.findAll(specification, pageable);
    }

}
