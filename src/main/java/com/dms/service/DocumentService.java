package com.dms.service;

import com.dms.dto.DestinationDTO;
import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithDocumentsDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.FileWithPathAlreadyExistsException;
import com.dms.exception.UnauthorizedAccessException;
import com.dms.mapper.dto.DocumentDTOMapper;
import com.dms.mapper.dto.PageWithDocumentsDTOMapper;
import com.dms.mapper.dto.PageWithRevisionsDTOMapper;
import com.dms.repository.DocumentRepository;
import com.dms.specification.DocumentFilterSpecification;
import com.dms.specification.RevisionFilterSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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

    private boolean isDocumentCreatedByAuthUser(Document document) {
        String documentAuthorEmail = document.getAuthor().getEmail();
        String authenticatedUserEmail = userService.getAuthenticatedUserEmail();
        return documentAuthorEmail.equals(authenticatedUserEmail);
    }

    public Document getDocumentWithAuthCheck(String documentId) {
        log.debug("Getting document: documentId={}", documentId);
        Document document = documentRepository.findByDocumentId(documentId)
                                              .orElseThrow(() -> new DocumentNotFoundException("Document with ID: " + documentId + " not found"));

        if (!isDocumentCreatedByAuthUser(document)) {
            throw new UnauthorizedAccessException("Can't access document of someone else");
        }

        return document;
    }

    public DocumentDTO getDocument(String documentId) {
        log.debug("Request - Getting document: documentId={}", documentId);

        Document document = getDocumentWithAuthCheck(documentId);
        log.info("Document {} retrieved successfully", documentId);

        return DocumentDTOMapper.map(document);
    }

    private Document createDocument(MultipartFile file, String path) {
        String hash = documentCommonService.storeBlob(file);
        User author = userService.getAuthenticatedUser();

        String name = getFilename(file);
        String type = file.getContentType();

        log.info("Document {} successfully created (not persisted yet)", name);

        return Document.builder()
                       .name(name)
                       .type(type)
                       .path(path)
                       .hash(hash)
                       .version(1L)
                       .author(author)
                       .build();
    }

    private static String getFilename(MultipartFile file) {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String cleanPath = StringUtils.cleanPath(originalFileName);
        return StringUtils.getFilename(cleanPath);
    }

    private void validateUniquePath(String path, Document document) {
        log.debug("Validating unique path: path={}, document={}", path, document);

        String filename = document.getName();
        User author = document.getAuthor();

        // user can't have a duplicate path for a document with the same name
        if (documentRepository.pathWithFileAlreadyExists(path, filename, author)) {
            throw new FileWithPathAlreadyExistsException("Document: " + filename + " with path: " + path + " already exists");
        }
    }

    @Transactional
    public DocumentDTO uploadDocument(MultipartFile file, DestinationDTO destination) {
        log.debug("Request - Uploading document: file={}, destination={}", file.getOriginalFilename(), destination);

        String path = destination.getPath();
        Document document = createDocument(file, path);

        validateUniquePath(path, document);

        // flush to immediately initialize the "createdAt" and "updatedAt" fields, ensuring the DTO does not contain null values for these properties
        Document savedDocument = documentRepository.saveAndFlush(document);

        log.info("Document {} with ID {} uploaded successfully", document.getName(), document.getDocumentId());

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return DocumentDTOMapper.map(savedDocument);
    }

    @Transactional
    public DocumentDTO uploadNewDocumentVersion(String documentId, MultipartFile file, DestinationDTO destination) {
        log.debug("Request - Uploading new document version: documentId={}, file={}, destination={}", documentId, file.getOriginalFilename(), destination);

        Document databaseDocument = getDocumentWithAuthCheck(documentId);
        String path = destination.getPath();

        Document document = createDocument(file, path);
        document.setId(databaseDocument.getId());
        document.setDocumentId(documentId);
        document.setVersion(documentCommonService.getLastRevisionVersion(document) + 1);
        document.setCreatedAt(databaseDocument.getCreatedAt());

        validateUniquePath(path, document);

        // flush to immediately initialize the "updatedAt" field, ensuring the DTO does not contain null values for this property
        Document savedDocument = documentRepository.saveAndFlush(document);

        log.info("Successfully uploaded new document version for document {}", documentId);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return DocumentDTOMapper.map(savedDocument);
    }

    @Transactional
    public DocumentDTO switchToRevision(String documentId, String revisionId) {
        log.debug("Request - Switching document to revision: documentId={}, revision={}", documentId, revisionId);

        Document document = getDocumentWithAuthCheck(documentId);
        DocumentRevision revision = documentCommonService.getRevisionByDocumentAndId(document, revisionId);

        Document documentFromRevision = documentCommonService.updateDocumentToRevision(document, revision);

        log.info("Successfully switched document {} to revision {}", documentId, revisionId);

        return DocumentDTOMapper.map(documentFromRevision);
    }

    @Transactional
    public void deleteDocumentWithRevisions(String documentId) {
        log.debug("Request - Deleting document with revisions: documentId={}", documentId);

        Document document = getDocumentWithAuthCheck(documentId);
        documentCommonService.deleteDocumentWithRevisions(document);

        log.info("Document {} with revisions deleted successfully", documentId);
    }

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        log.debug("Request - Downloading document: documentId={}", documentId);

        Document document = getDocumentWithAuthCheck(documentId);
        String hash = document.getHash();
        Resource file = documentCommonService.getBlob(hash);

        log.info("Document {} downloaded successfully", documentId);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(document.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                             .header(HttpHeaders.CONTENT_LENGTH, documentCommonService.getContentLength(file))
                             .body(file);
    }

    public PageWithDocumentsDTO getDocuments(int pageNumber, int pageSize, String sort, String filter) {
        log.debug("Request - Listing documents: pageNumber={}, pageSize={}, sort={}, filter={}", pageNumber, pageSize, sort, filter);

        User user = userService.getAuthenticatedUser();

        List<Sort.Order> sortOrders = documentCommonService.getDocumentSortOrders(sort);
        Map<String, String> filters = documentCommonService.getDocumentFilters(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<Document> specification = DocumentFilterSpecification.filter(filters, user);

        Page<DocumentDTO> documentDTOs = findDocuments(specification, pageable);

        log.info("Documents listed successfully");

        return PageWithDocumentsDTOMapper.map(documentDTOs);
    }

    private Page<DocumentDTO> findDocuments(Specification<Document> specification, Pageable pageable) {
        Page<Document> documents = documentRepository.findAll(specification, pageable);
        List<DocumentDTO> documentDTOs = documents.stream()
                                                  .map(DocumentDTOMapper::map)
                                                  .toList();

        return new PageImpl<>(documentDTOs, pageable, documents.getTotalElements());
    }

    public PageWithRevisionsDTO getDocumentRevisions(String documentId, int pageNumber, int pageSize, String sort, String filter) {
        log.debug("Request - Listing document revisions: documentId={} pageNumber={}, pageSize={}, sort={}, filter={}", documentId, pageNumber, pageSize, sort, filter);

        Document document = getDocumentWithAuthCheck(documentId);
        User user = userService.getAuthenticatedUser();

        List<Sort.Order> sortOrders = documentCommonService.getRevisionSortOrders(sort);
        Map<String, String> filters = documentCommonService.getRevisionFilters(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<DocumentRevision> specification = RevisionFilterSpecification.filterByDocument(document, filters, user);

        Page<DocumentRevisionDTO> documentRevisionDTOs = documentCommonService.findRevisions(specification, pageable);

        log.info("Document revisions listed successfully");

        return PageWithRevisionsDTOMapper.map(documentRevisionDTOs);
    }

    @Transactional
    public DocumentDTO moveDocument(String documentId, DestinationDTO destination) {
        log.debug("Request - Moving document: documentId={}, destination={}", documentId, destination);

        Document document = getDocumentWithAuthCheck(documentId);
        String path = destination.getPath();

        validateUniquePath(path, document);

        document.setPath(path);
        Document savedDocument = documentRepository.save(document);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        log.info("Document {} moved successfully to path {}", documentId, path);

        return DocumentDTOMapper.map(savedDocument);
    }

}
