package com.dms.service;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.DocumentWithVersionDTO;
import com.dms.dto.PageWithDocumentsDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.dto.PageWithVersionsDTO;
import com.dms.dto.PathRequestDTO;
import com.dms.dto.UserRequestDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.FileWithPathAlreadyExistsException;
import com.dms.filter.FilterItem;
import com.dms.mapper.dto.DocumentDTOMapper;
import com.dms.mapper.dto.DocumentWithVersionDTOMapper;
import com.dms.mapper.dto.PageWithDocumentsDTOMapper;
import com.dms.mapper.dto.PageWithRevisionsDTOMapper;
import com.dms.repository.DocumentRepository;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    private final DocumentCommonService documentCommonService;
    private final UserService userService;

    public DocumentDTO getDocument(String documentId) {
        Document document = documentCommonService.getDocument(documentId);
        return DocumentDTOMapper.map(document);
    }

    public DocumentWithVersionDTO getDocumentWithVersion(String documentId, Long version) {
        Document document = documentCommonService.getDocument(documentId);
        DocumentRevision revision = documentCommonService.getRevisionByDocumentAndVersion(document, version);

        return DocumentWithVersionDTOMapper.map(document, revision);
    }

    private LocalDateTime getDocumentCreatedAt(String documentId) {
        return documentRepository.getCreatedAtByDocumentId(documentId)
                                 .orElseThrow(() -> new RuntimeException("Creation time not found for file with ID: " + documentId));
    }

    private User getUserFromUserRequest(UserRequestDTO userRequest) {
        User user = User.builder()
                        .username(userRequest.getUsername())
                        .email(userRequest.getEmail())
                        .build();

        return userService.getSavedUser(user);
    }

    private String getPathFromRequest(PathRequestDTO pathRequest) {
        return pathRequest == null ? null : pathRequest.getPath();
    }

    private Document createDocument(UserRequestDTO userRequest, MultipartFile file, String path) {
        String hash = documentCommonService.storeBlob(file);
        User author = getUserFromUserRequest(userRequest);

        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String cleanPath = StringUtils.cleanPath(originalFileName);
        String name = StringUtils.getFilename(cleanPath);
        String type = file.getContentType();

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
        String filename = document.getName();
        User author = document.getAuthor();

        // user can't have a duplicate path for a document with the same name
        if (documentCommonService.pathWithFileAlreadyExists(path, filename, author))
            throw new FileWithPathAlreadyExistsException("Document: " + filename + " with path: " + path + " already exists");
    }

    @Transactional
    public DocumentDTO uploadDocument(UserRequestDTO userRequest, MultipartFile file, PathRequestDTO pathRequest) {
        String path = getPathFromRequest(pathRequest);
        Document document = createDocument(userRequest, file, path);

        validateUniquePath(path, document);

        // flush to immediately initialize the "createdAt" and "updatedAt" fields, ensuring the DTO does not contain null values for these properties
        Document savedDocument = documentRepository.saveAndFlush(document);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return DocumentDTOMapper.map(savedDocument);
    }

    @Transactional
    public DocumentDTO uploadNewDocumentVersion(String documentId, UserRequestDTO userRequest, MultipartFile file, PathRequestDTO pathRequest) {
        if (!documentRepository.existsByDocumentId(documentId))
            throw new DocumentNotFoundException("File with ID: " + documentId + " not found for replacement");

        Document databaseDocument = documentCommonService.getDocument(documentId);
        String path = getPathFromRequest(pathRequest);

        Document document = createDocument(userRequest, file, path);
        document.setId(databaseDocument.getId());
        document.setDocumentId(documentId);
        document.setVersion(documentCommonService.getLastRevisionVersion(document) + 1);

        validateUniquePath(path, document);

        // flush to immediately initialize the "updatedAt" field, ensuring the DTO does not contain null values for this property
        Document savedDocument = documentRepository.saveAndFlush(document);

        // createdAt column is not initialized because of "updatable = false" -> set it manually
        LocalDateTime createdAt = getDocumentCreatedAt(documentId);
        savedDocument.setCreatedAt(createdAt);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return DocumentDTOMapper.map(savedDocument);
    }

    @Transactional
    public DocumentDTO switchToVersion(String documentId, Long version) {
        Document document = documentCommonService.getDocument(documentId);
        DocumentRevision revision = documentCommonService.getRevisionByDocumentAndVersion(document, version);

        Document documentFromRevision = documentCommonService.updateDocumentToRevision(document, revision);

        return DocumentDTOMapper.map(documentFromRevision);
    }

    @Transactional
    public DocumentDTO switchToRevision(String documentId, String revisionId) {
        Document document = documentCommonService.getDocument(documentId);
        DocumentRevision revision = documentCommonService.getRevisionByDocumentAndId(document, revisionId);

        Document documentFromRevision = documentCommonService.updateDocumentToRevision(document, revision);

        return DocumentDTOMapper.map(documentFromRevision);
    }

    @Transactional
    public void deleteDocumentWithRevisions(String documentId) {
        Document document = documentCommonService.getDocument(documentId);

        List<DocumentRevision> documentRevisions = document.getRevisions();
        documentRevisions.forEach(revision -> documentCommonService.deleteBlobIfDuplicateHashNotExists(revision.getHash()));

        documentCommonService.deleteBlobIfDuplicateHashNotExists(document.getHash());
        documentRepository.delete(document);
    }

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        Document document = documentCommonService.getDocument(documentId);
        String hash = document.getHash();
        byte[] data = documentCommonService.getBlob(hash);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(document.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                             .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(data.length))
                             .body(new ByteArrayResource(data));
    }

    public PageWithDocumentsDTO getDocuments(int pageNumber, int pageSize, String sort, String filter) {
        List<Sort.Order> sortOrders = documentCommonService.getDocumentOrders(sort);
        List<FilterItem> filterItems = documentCommonService.getDocumentFilterItems(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<Document> specification = DocumentFilterSpecification.filterByItems(filterItems);

        Page<DocumentDTO> documentDTOs = findDocuments(specification, pageable);
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
        Document document = documentCommonService.getDocument(documentId);

        List<Sort.Order> orders = documentCommonService.getRevisionOrders(sort);
        List<FilterItem> filterItems = documentCommonService.getRevisionFilterItems(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(orders));
        Specification<DocumentRevision> specification = DocumentFilterSpecification.filterByDocumentAndFilterItems(document, filterItems);

        Page<DocumentRevisionDTO> documentRevisionDTOs = documentCommonService.findRevisions(specification, pageable);
        return PageWithRevisionsDTOMapper.map(documentRevisionDTOs);
    }

    public PageWithVersionsDTO getDocumentVersions(String documentId, int pageNumber, int pageSize) {
        Document document = documentCommonService.getDocument(documentId);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return documentCommonService.getDocumentVersions(document, pageable);
    }

    @Transactional
    public DocumentDTO moveDocument(String documentId, PathRequestDTO pathRequest) {
        Document document = documentCommonService.getDocument(documentId);
        String path = getPathFromRequest(pathRequest);

        validateUniquePath(path, document);

        document.setPath(path);
        Document savedDocument = documentRepository.save(document);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return DocumentDTOMapper.map(savedDocument);
    }

}
