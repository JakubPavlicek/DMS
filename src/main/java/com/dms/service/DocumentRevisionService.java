package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.RevisionDeletionException;
import com.dms.exception.RevisionNotFoundException;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.specification.RevisionFilterSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
import java.util.Map;
import java.util.Optional;

/**
 * Service class responsible for managing document revisions.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class DocumentRevisionService {

    /** Repository for accessing document revisions. */
    private final DocumentRevisionRepository revisionRepository;

    /** Service for common document operations. */
    private final DocumentCommonService documentCommonService;
    /** Service for managing user-related operations. */
    private final UserService userService;

    /**
     * Retrieves the revision corresponding to the given ID if the authenticated user has access.
     *
     * @param revisionId the ID of the revision to retrieve
     * @return the revision corresponding to the ID
     * @throws RevisionNotFoundException if the revision with the given ID is not found
     */
    private DocumentRevision getAuthenticatedUserRevision(String revisionId) {
        log.debug("Getting revision: revisionId={}", revisionId);
        User user = userService.getAuthenticatedUser();
        return revisionRepository.findByRevisionIdAndAuthor(revisionId, user)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revision with ID: " + revisionId + " not found"));
    }

    /**
     * Retrieves the revision corresponding to the given ID.
     *
     * @param revisionId the ID of the revision to retrieve
     * @return the revision corresponding to the ID
     */
    public DocumentRevision getRevision(String revisionId) {
        log.debug("Request - Getting revision: revisionId={}", revisionId);

        DocumentRevision revision = getAuthenticatedUserRevision(revisionId);
        log.info("Revision {} retrieved successfully", revisionId);

        return revision;
    }

    /**
     * Replaces the given document with an adjacent revision, if available.
     *
     * @param document the document to replace
     * @throws RevisionDeletionException if no adjacent revision is found and the document cannot be replaced
     */
    private void replaceDocumentWithAdjacentRevision(Document document) {
        Long currentVersion = document.getVersion();

        Optional<DocumentRevision> previousRevision = revisionRepository.findPreviousByDocumentAndVersion(document, currentVersion);
        Optional<DocumentRevision> nextRevision = revisionRepository.findNextByDocumentAndVersion(document, currentVersion);

        DocumentRevision replacingRevision;

        if (previousRevision.isPresent()) {
            replacingRevision = previousRevision.get();
        }
        else if (nextRevision.isPresent()) {
            replacingRevision = nextRevision.get();
        }
        else {
            throw new RevisionDeletionException(
                "Revision cannot be deleted because this is the only version left for the document with ID: " + document.getDocumentId()
            );
        }

        log.debug("Replacing document with adjacent revision: document={}, revision={}", document, replacingRevision);

        documentCommonService.updateDocumentToRevision(document, replacingRevision);
    }

    /**
     * Checks if the given revision is set as the current version of the document.
     *
     * @param revision the revision to check
     * @param document the document to compare with
     * @return true if the revision is set as the current version of the document, false otherwise
     */
    private boolean isRevisionSetAsCurrent(DocumentRevision revision, Document document) {
        return revision.getVersion().equals(document.getVersion());
    }

    /**
     * Deletes the revision with the given ID.
     * If the deleted revision is also the current document version, it is replaced with an adjacent revision if available.
     * If the deleted revision is the last version of the document, the document's version is decremented.
     *
     * @param revisionId the ID of the revision to delete
     */
    @Transactional
    public void deleteRevision(String revisionId) {
        log.debug("Request - Deleting revision: revisionId={}", revisionId);

        DocumentRevision revision = getAuthenticatedUserRevision(revisionId);
        Document document = revision.getDocument();

        // revision which is also a current document is being deleted -> switch document to adjacent revision
        if (isRevisionSetAsCurrent(revision, document)) {
            replaceDocumentWithAdjacentRevision(document);
        }

        documentCommonService.safelyDeleteBlob(revision.getHash());
        revisionRepository.deleteByRevisionId(revisionId);

        documentCommonService.updateRevisionVersionsForDocument(document);

        // document's previous version was deleted -> decrement current document's version
        if (hasRevisionLowerVersionThanDocument(revision, document)) {
            decrementDocumentVersion(document);
        }

        log.info("Revision {} deleted successfully", revisionId);
    }

    /**
     * Checks if the given revision has a lower version number than the document's current version.
     *
     * @param revision the revision to check
     * @param document the document to compare with
     * @return true if the revision has a lower version number than the document's current version, false otherwise
     */
    private boolean hasRevisionLowerVersionThanDocument(DocumentRevision revision, Document document) {
        return revision.getVersion().compareTo(document.getVersion()) < 0;
    }

    /**
     * Decrements the version number of the given document by one and saves the updated document.
     *
     * @param document the document whose version is to be decremented
     */
    private void decrementDocumentVersion(Document document) {
        document.setVersion(document.getVersion() - 1);
        documentCommonService.saveDocument(document);
    }

    /**
     * Retrieves the file from the blob storage service based on the hash of the revision, and returns it as a {@link ResponseEntity}.
     *
     * @param revisionId the ID of the revision to download
     * @return {@link ResponseEntity} containing the downloaded document revision as a {@link Resource}
     */
    public ResponseEntity<Resource> downloadRevision(String revisionId) {
        log.debug("Request - Downloading revision: revisionId={}", revisionId);

        DocumentRevision revision = getAuthenticatedUserRevision(revisionId);
        Resource file = documentCommonService.getBlob(revision.getHash());
        String contentLength = documentCommonService.getContentLength(file);

        log.info("Revision {} downloaded successfully", revisionId);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(revision.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + revision.getName() + "\"")
                             .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                             .body(file);
    }

    /**
     * Retrieves a page of document revisions based on the provided parameters.
     *
     * @param pageNumber the page number of the revisions to retrieve
     * @param pageSize the number of revisions per page
     * @param sort the sorting criteria for the revisions
     * @param filter the filter criteria for the revisions
     * @return a {@link Page} containing the requested revisions
     */
    public Page<DocumentRevision> getRevisions(int pageNumber, int pageSize, String sort, String filter) {
        log.debug("Request - Listing revisisons: pageNumber={}, pageSize={}, sort={}, filter={}", pageNumber, pageSize, sort, filter);

        User user = userService.getAuthenticatedUser();

        List<Sort.Order> sortOrders = documentCommonService.getRevisionSortOrders(sort);
        Map<String, String> filters = documentCommonService.getRevisionFilters(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<DocumentRevision> specification = RevisionFilterSpecification.filter(filters, user);

        Page<DocumentRevision> documentRevisions = documentCommonService.findRevisions(specification, pageable);

        log.info("Revisions listed successfully");

        return documentRevisions;
    }

}