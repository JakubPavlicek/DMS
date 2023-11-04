package com.dms.service;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.exception.RevisionDeletionException;
import com.dms.filter.FilterItem;
import com.dms.mapper.dto.DocumentRevisionDTOMapper;
import com.dms.mapper.dto.PageWithRevisionsDTOMapper;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.specification.DocumentFilterSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class DocumentRevisionService {

    private final DocumentRevisionRepository revisionRepository;

    private final DocumentCommonService documentCommonService;

    public DocumentRevisionDTO getRevision(String revisionId) {
        log.debug("Request - Getting revision: revisionId={}", revisionId);

        DocumentRevision revision = documentCommonService.getRevision(revisionId);
        log.info("Revision {} retrieved successfully", revisionId);

        return DocumentRevisionDTOMapper.map(revision);
    }

    private void replaceDocumentWithAdjacentRevision(Document document) {
        Long currentVersion = document.getVersion();
        DocumentRevision replacingRevision = revisionRepository.findPreviousByDocumentAndVersion(document, currentVersion)
                                                               .orElse(revisionRepository.findNextByDocumentAndVersion(document, currentVersion)
                                                                                         .orElse(null));

        log.debug("Replacing document with adjacent revision: document={}, replacingRevision={}", document, replacingRevision);

        if (replacingRevision == null)
            throw new RevisionDeletionException(
                "Revision cannnot be deleted because this is the only version left for the document with ID: " + document.getDocumentId()
            );

        documentCommonService.updateDocumentToRevision(document, replacingRevision);
    }

    private boolean isRevisionSetAsCurrent(DocumentRevision revision, Document document) {
        return revision.getVersion()
                       .equals(document.getVersion());
    }

    @Transactional
    public void deleteRevision(String revisionId) {
        log.debug("Request - Deleting revision: revisionId={}", revisionId);

        DocumentRevision documentRevision = documentCommonService.getRevision(revisionId);
        Document document = documentRevision.getDocument();

        if (isRevisionSetAsCurrent(documentRevision, document))
            replaceDocumentWithAdjacentRevision(document);

        documentCommonService.deleteBlobIfDuplicateHashNotExists(documentRevision.getHash());
        revisionRepository.deleteByRevisionId(revisionId);

        documentCommonService.updateRevisionVersionsForDocument(document);

        log.info("Revision {} deleted successfully", revisionId);
    }

    public ResponseEntity<Resource> downloadRevision(String revisionId) {
        log.debug("Request - Downloading revision: revisionId={}", revisionId);

        DocumentRevision revision = documentCommonService.getRevision(revisionId);
        String hash = revision.getHash();
        byte[] data = documentCommonService.getBlob(hash);

        log.info("Revision {} downloaded successfully", revisionId);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(revision.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + revision.getName() + "\"")
                             .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(data.length))
                             .body(new ByteArrayResource(data));
    }

    public PageWithRevisionsDTO getRevisions(int pageNumber, int pageSize, String sort, String filter) {
        log.debug("Request - Listing revisisons: pageNumber={}, pageSize={}, sort={}, filter={}", pageNumber, pageSize, sort, filter);

        List<Sort.Order> orders = documentCommonService.getRevisionOrders(sort);
        List<FilterItem> filterItems = documentCommonService.getRevisionFilterItems(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(orders));
        Specification<DocumentRevision> specification = DocumentFilterSpecification.filterByItems(filterItems);

        Page<DocumentRevisionDTO> documentRevisionDTOs = documentCommonService.findRevisions(specification, pageable);

        log.info("Revisions listed successfully");

        return PageWithRevisionsDTOMapper.map(documentRevisionDTOs);
    }

}
