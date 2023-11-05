package com.dms.service;

import com.dms.dto.DestinationDTO;
import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithDocumentsDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.dto.UserDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.FileWithPathAlreadyExistsException;
import com.dms.exception.InvalidRegexInputException;
import com.dms.mapper.dto.DocumentDTOMapper;
import com.dms.mapper.dto.PageWithDocumentsDTOMapper;
import com.dms.mapper.dto.PageWithRevisionsDTOMapper;
import com.dms.mapper.dto.UserDTOMapper;
import com.dms.repository.DocumentRepository;
import com.dms.specification.DocumentFilterSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

    private static final String PATH_REGEX = "/$|(/[\\w\\-]+)+";

    private final DocumentRepository documentRepository;

    private final DocumentCommonService documentCommonService;
    private final UserService userService;

    public DocumentDTO getDocument(String documentId) {
        log.debug("Request - Getting document: documentId={}", documentId);

        Document document = documentCommonService.getDocument(documentId);
        log.info("Document {} retrieved successfully", documentId);

        return DocumentDTOMapper.map(document);
    }

    private LocalDateTime getDocumentCreatedAt(String documentId) {
        return documentRepository.getCreatedAtByDocumentId(documentId)
                                 .orElseThrow(() -> new RuntimeException("Creation time not found for file with ID: " + documentId));
    }

    private User getUserFromUserDTO(UserDTO userDTO) {
        User user = UserDTOMapper.mapToUser(userDTO);
        return userService.getSavedUser(user);
    }

    private String getPathFromRequest(DestinationDTO destination) {
        if (destination == null)
            return null;

        String path = destination.getPath();

        if (!path.matches(PATH_REGEX))
            throw new InvalidRegexInputException("Request part 'path' does not match the expected format");

        return path;
    }

    private Document createDocument(UserDTO user, MultipartFile file, String path) {
        String hash = documentCommonService.storeBlob(file);
        User author = getUserFromUserDTO(user);

        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String cleanPath = StringUtils.cleanPath(originalFileName);
        String name = StringUtils.getFilename(cleanPath);
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

    private void validateUniquePath(String path, Document document) {
        log.debug("Validating unique path: path={}, document={}", path, document);

        String filename = document.getName();
        User author = document.getAuthor();

        // user can't have a duplicate path for a document with the same name
        if (documentCommonService.pathWithFileAlreadyExists(path, filename, author))
            throw new FileWithPathAlreadyExistsException("Document: " + filename + " with path: " + path + " already exists");
    }

    @Transactional
    public DocumentDTO uploadDocument(UserDTO user, MultipartFile file, DestinationDTO destination) {
        log.debug("Request - Uploading document: user={}, file={}, destination={}", user, file.getOriginalFilename(), destination);

        String path = getPathFromRequest(destination);
        Document document = createDocument(user, file, path);

        validateUniquePath(path, document);

        // flush to immediately initialize the "createdAt" and "updatedAt" fields, ensuring the DTO does not contain null values for these properties
        Document savedDocument = documentRepository.saveAndFlush(document);

        log.info("Document {} with ID {} uploaded successfully", document.getName(), document.getDocumentId());

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return DocumentDTOMapper.map(savedDocument);
    }

    @Transactional
    public DocumentDTO uploadNewDocumentVersion(String documentId, UserDTO user, MultipartFile file, DestinationDTO destination) {
        log.debug("Request - Uploading new document version: documentId={}, user={}, file={}, destination={}", documentId, user, file.getOriginalFilename(), destination);

        if (!documentRepository.existsByDocumentId(documentId))
            throw new DocumentNotFoundException("File with ID: " + documentId + " not found for replacement");

        Document databaseDocument = documentCommonService.getDocument(documentId);
        String path = getPathFromRequest(destination);

        Document document = createDocument(user, file, path);
        document.setId(databaseDocument.getId());
        document.setDocumentId(documentId);
        document.setVersion(documentCommonService.getLastRevisionVersion(document) + 1);

        validateUniquePath(path, document);

        // flush to immediately initialize the "updatedAt" field, ensuring the DTO does not contain null values for this property
        Document savedDocument = documentRepository.saveAndFlush(document);

        // createdAt column is not initialized because of "updatable = false" -> set it manually
        LocalDateTime createdAt = getDocumentCreatedAt(documentId);
        savedDocument.setCreatedAt(createdAt);

        log.info("Successfully uploaded new document version for document {}", documentId);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return DocumentDTOMapper.map(savedDocument);
    }

    @Transactional
    public DocumentDTO switchToRevision(String documentId, String revisionId) {
        log.debug("Request - Switching document to revision: documentId={}, revision={}", documentId, revisionId);

        Document document = documentCommonService.getDocument(documentId);
        DocumentRevision revision = documentCommonService.getRevisionByDocumentAndId(document, revisionId);

        Document documentFromRevision = documentCommonService.updateDocumentToRevision(document, revision);

        log.info("Successfully switched document {} to revision {}", documentId, revisionId);

        return DocumentDTOMapper.map(documentFromRevision);
    }

    @Transactional
    public void deleteDocumentWithRevisions(String documentId) {
        log.debug("Request - Deleting document with revisions: documentId={}", documentId);

        Document document = documentCommonService.getDocument(documentId);

        List<DocumentRevision> documentRevisions = document.getRevisions();
        documentRevisions.forEach(revision -> documentCommonService.deleteBlobIfDuplicateHashNotExists(revision.getHash()));

        documentCommonService.deleteBlobIfDuplicateHashNotExists(document.getHash());
        documentRepository.delete(document);

        log.info("Document {} with revisions deleted successfully", documentId);
    }

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        log.debug("Request - Downloading document: documentId={}", documentId);

        Document document = documentCommonService.getDocument(documentId);
        String hash = document.getHash();
        byte[] data = documentCommonService.getBlob(hash);

        log.info("Document {} downloaded successfully", documentId);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(document.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                             .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(data.length))
                             .body(new ByteArrayResource(data));
    }

    public PageWithDocumentsDTO getDocuments(int pageNumber, int pageSize, String sort, String filter) {
        log.debug("Request - Listing documents: pageNumber={}, pageSize={}, sort={}, filter={}", pageNumber, pageSize, sort, filter);

        List<Sort.Order> sortOrders = documentCommonService.getDocumentSortOrders(sort);
        Map<String, String> filters = documentCommonService.getDocumentFilters(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<Document> specification = DocumentFilterSpecification.filterByItems(filters);

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

        Document document = documentCommonService.getDocument(documentId);

        List<Sort.Order> sortOrders = documentCommonService.getRevisionSortOrders(sort);
        Map<String, String> filters = documentCommonService.getRevisionFilters(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<DocumentRevision> specification = DocumentFilterSpecification.filterByDocumentAndFilterItems(document, filters);

        Page<DocumentRevisionDTO> documentRevisionDTOs = documentCommonService.findRevisions(specification, pageable);

        log.info("Document revisions listed successfully");

        return PageWithRevisionsDTOMapper.map(documentRevisionDTOs);
    }

    @Transactional
    public DocumentDTO moveDocument(String documentId, DestinationDTO destination) {
        log.debug("Request - Moving document: documentId={}, destination={}", documentId, destination);

        Document document = documentCommonService.getDocument(documentId);
        String path = getPathFromRequest(destination);

        validateUniquePath(path, document);

        document.setPath(path);
        Document savedDocument = documentRepository.save(document);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        log.info("Document {} moved successfully to path {}", documentId, path);

        return DocumentDTOMapper.map(savedDocument);
    }

}
