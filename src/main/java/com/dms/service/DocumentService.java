package com.dms.service;

import com.dms.config.ArchiveProperties;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.FileWithPathAlreadyExistsException;
import com.dms.repository.DocumentRepository;
import com.dms.specification.DocumentFilterSpecification;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Log4j2
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    private final DocumentCommonService documentCommonService;
    private final UserService userService;

    private final ArchiveProperties archiveProperties;

    private Document getAuthenticatedUserDocument(String documentId) {
        log.debug("Getting document: documentId={}", documentId);
        User user = userService.getAuthenticatedUser();
        return documentRepository.findByDocumentIdAndAuthor(documentId, user)
                                 .orElseThrow(() -> new DocumentNotFoundException("Document with ID: " + documentId + " not found"));
    }

    public Document getDocument(String documentId) {
        log.debug("Request - Getting document: documentId={}", documentId);

        Document document = getAuthenticatedUserDocument(documentId);
        log.info("Document {} retrieved successfully", documentId);

        return document;
    }

    private Document createDocument(MultipartFile file, String path) {
        String hash = documentCommonService.storeBlob(file);
        User author = userService.getAuthenticatedUser();

        String name = getFilename(file);
        String type = file.getContentType();
        Long size = file.getSize();

        log.info("Document {} successfully created (not persisted yet)", name);

        return Document.builder()
                       .name(name)
                       .type(type)
                       .path(path)
                       .size(size)
                       .hash(hash)
                       .version(1L)
                       .isArchived(false)
                       .author(author)
                       .build();
    }

    private static String getFilename(MultipartFile file) {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String cleanPath = StringUtils.cleanPath(originalFileName);
        return StringUtils.getFilename(cleanPath);
    }

    private void ensureUniquePath(String path, String fileName) {
        log.debug("Ensuring unique document path: path={}, fileName={}", path, fileName);

        User author = userService.getAuthenticatedUser();

        // user can't have a duplicate path for a document with the same name
        if (documentRepository.documentWithPathAlreadyExists(fileName, path, author)) {
            throw new FileWithPathAlreadyExistsException("Document: " + fileName + " with path: " + path + " already exists");
        }
    }

    @Transactional
    public Document uploadDocument(MultipartFile file, String path) {
        log.debug("Request - Uploading document: file={}, path={}", file.getOriginalFilename(), path);

        String fileName = getFilename(file);

        ensureUniquePath(path, fileName);

        Document document = createDocument(file, path);

        Document savedDocument = documentRepository.save(document);

        log.info("Document {} with ID {} uploaded successfully", document.getName(), document.getDocumentId());

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return savedDocument;
    }

    @Transactional
    public Document uploadNewDocumentVersion(String documentId, MultipartFile file, String path) {
        log.debug("Request - Uploading new document version: documentId={}, file={}, path={}", documentId, file.getOriginalFilename(), path);

        Document oldDocument = getAuthenticatedUserDocument(documentId);
        String fileName = getFilename(file);

        // path was not provided -> use old (existing) path
        if (path == null) {
            path = oldDocument.getPath();
        }
        else {
            ensureUniquePath(path, fileName);
        }

        Document newDocument = createNewDocumentVersion(oldDocument, file, path);

        Document savedDocument = documentRepository.save(newDocument);

        log.info("Successfully uploaded new document version for document {}", documentId);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return savedDocument;
    }

    private Document createNewDocumentVersion(Document oldDocument, MultipartFile file, String path) {
        Document newDocument = createDocument(file, path);
        newDocument.setId(oldDocument.getId());
        newDocument.setDocumentId(oldDocument.getDocumentId());
        newDocument.setVersion(documentCommonService.getLastRevisionVersion(newDocument) + 1);
        newDocument.setCreatedAt(oldDocument.getCreatedAt());

        return newDocument;
    }

    @Transactional
    public Document switchToRevision(String documentId, String revisionId) {
        log.debug("Request - Switching document to revision: documentId={}, revision={}", documentId, revisionId);

        Document document = getAuthenticatedUserDocument(documentId);
        DocumentRevision revision = documentCommonService.getRevisionByDocumentAndId(document, revisionId);

        Document documentFromRevision = documentCommonService.updateDocumentToRevision(document, revision);

        log.info("Successfully switched document {} to revision {}", documentId, revisionId);

        return documentFromRevision;
    }

    @Transactional
    public void deleteDocumentWithRevisions(String documentId) {
        log.debug("Request - Deleting document with revisions: documentId={}", documentId);

        Document document = getAuthenticatedUserDocument(documentId);

        Integer revisionCount = documentCommonService.getRevisionCountForDocument(document);

        int pageSize = 10;
        int pageCount = (int) Math.ceil((double) revisionCount / pageSize);

        for (int i = 0; i < pageCount; i++) {
            Pageable pageable = PageRequest.of(i, pageSize);
            Page<DocumentRevision> revisions = documentCommonService.findAllRevisionsByDocument(document, pageable);

            revisions.forEach(revision -> documentCommonService.safelyDeleteBlob(revision.getHash()));
        }

        documentRepository.delete(document);

        log.info("Document {} with revisions deleted successfully", documentId);
    }

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        log.debug("Request - Downloading document: documentId={}", documentId);

        Document document = getAuthenticatedUserDocument(documentId);
        Resource file = documentCommonService.getBlob(document.getHash());
        String contentLength = documentCommonService.getContentLength(file);

        log.info("Document {} downloaded successfully", documentId);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(document.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                             .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                             .body(file);
    }

    public Page<Document> getDocuments(int pageNumber, int pageSize, String sort, String filter) {
        log.debug("Request - Listing documents: pageNumber={}, pageSize={}, sort={}, filter={}", pageNumber, pageSize, sort, filter);

        User user = userService.getAuthenticatedUser();

        List<Sort.Order> sortOrders = documentCommonService.getDocumentSortOrders(sort);
        Map<String, String> filters = documentCommonService.getDocumentFilters(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<Document> specification = DocumentFilterSpecification.filterByUser(filters, user);

        Page<Document> documents = documentRepository.findAll(specification, pageable);

        log.info("Documents listed successfully");

        return documents;
    }

    public Page<DocumentRevision> getDocumentRevisions(String documentId, int pageNumber, int pageSize, String sort, String filter) {
        log.debug("Request - Listing document revisions: documentId={} pageNumber={}, pageSize={}, sort={}, filter={}", documentId, pageNumber, pageSize, sort, filter);

        Document document = getAuthenticatedUserDocument(documentId);

        List<Sort.Order> sortOrders = documentCommonService.getRevisionSortOrders(sort);
        Map<String, String> filters = documentCommonService.getRevisionFilters(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<DocumentRevision> specification = RevisionFilterSpecification.filterByDocument(document, filters, document.getAuthor());

        Page<DocumentRevision> documentRevisions = documentCommonService.findRevisions(specification, pageable);

        log.info("Document revisions listed successfully");

        return documentRevisions;
    }

    @Transactional
    public Document moveDocument(String documentId, String path) {
        log.debug("Request - Moving document: documentId={}, path={}", documentId, path);

        Document document = getAuthenticatedUserDocument(documentId);

        ensureUniquePath(path, document.getName());

        document.setPath(path);
        Document savedDocument = documentRepository.save(document);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        log.info("Document {} moved successfully to path {}", documentId, path);

        return savedDocument;
    }

    @Transactional
    public void archiveDocument(String documentId) {
        log.debug("Request - Archiving document: documentId={}", documentId);

        Document document = getAuthenticatedUserDocument(documentId);
        int retentionDays = archiveProperties.getRetentionPeriodInDays();

        document.setIsArchived(true);
        document.setDeleteAt(LocalDateTime.now().plusDays(retentionDays));
        documentRepository.save(document);

        log.info("Document {} archived successfully", documentId);
    }

    @Transactional
    public Document restoreDocument(String documentId) {
        log.debug("Request - Restoring document: documentId={}", documentId);

        Document document = getAuthenticatedUserDocument(documentId);

        document.setIsArchived(false);
        document.setDeleteAt(null);
        Document restoredDocument = documentRepository.save(document);

        log.info("Document {} restored successfully", documentId);

        return restoredDocument;
    }

}
