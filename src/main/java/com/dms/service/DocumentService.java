package com.dms.service;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.DocumentWithVersionDTO;
import com.dms.dto.UserRequest;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.filter.FilterItem;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    private final DocumentCommonService documentCommonService;
    private final UserService userService;

    public DocumentDTO getDocument(UUID documentId) {
        Document document = documentCommonService.getDocument(documentId);
        return documentCommonService.mapDocumentToDocumentDto(document);
    }

    public DocumentWithVersionDTO getDocumentWithVersion(UUID documentId, Long version) {
        Document document = documentCommonService.getDocument(documentId);
        DocumentRevision revision = documentCommonService.getRevisionByDocumentAndVersion(document, version);

        return DocumentWithVersionDTO.builder()
                                     .documentId(document.getDocumentId())
                                     .version(revision.getVersion())
                                     .author(documentCommonService.mapUserToUserDto(revision.getAuthor()))
                                     .name(revision.getName())
                                     .type(revision.getType())
                                     .path(revision.getPath())
                                     .createdAt(revision.getCreatedAt())
                                     .build();
    }

    private LocalDateTime getDocumentCreatedAt(UUID documentId) {
        return documentRepository.getCreatedAtByDocumentId(documentId)
                                 .orElseThrow(() -> new RuntimeException("Creation time not found for file with ID: " + documentId));
    }

    private User getUserFromUserRequest(UserRequest userRequest) {
        User user = User.builder()
                        .username(userRequest.getUsername())
                        .email(userRequest.getEmail())
                        .build();

        return userService.getSavedUser(user);
    }

    private Document createDocument(UserRequest userRequest, MultipartFile file, String path) {
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
            throw new RuntimeException("File: " + filename + " with path: " + path + " already exists");
    }

    @Transactional
    public DocumentDTO uploadDocument(UserRequest userRequest, MultipartFile file, String path) {
        Document document = createDocument(userRequest, file, path);

        validateUniquePath(path, document);

        // flush to immediately initialize the "createdAt" and "updatedAt" fields, ensuring the DTO does not contain null values for these properties
        Document savedDocument = documentRepository.saveAndFlush(document);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return documentCommonService.mapDocumentToDocumentDto(savedDocument);
    }

    @Transactional
    public DocumentDTO uploadNewDocumentVersion(UUID documentId, UserRequest userRequest, MultipartFile file, String path) {
        if (!documentRepository.existsById(documentId))
            throw new DocumentNotFoundException("File with ID: " + documentId + " not found for replacement");

        Document document = createDocument(userRequest, file, path);
        document.setDocumentId(documentId);
        document.setVersion(documentCommonService.getLastRevisionVersion(document) + 1);

        validateUniquePath(path, document);

        // flush to immediately initialize the "updatedAt" field, ensuring the DTO does not contain null values for this property
        Document savedDocument = documentRepository.saveAndFlush(document);

        // createdAt column is not initialized because of "updatable = false" -> set it manually
        LocalDateTime createdAt = getDocumentCreatedAt(documentId);
        savedDocument.setCreatedAt(createdAt);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return documentCommonService.mapDocumentToDocumentDto(savedDocument);
    }

    @Transactional
    public DocumentDTO switchToVersion(UUID documentId, Long version) {
        Document document = documentCommonService.getDocument(documentId);
        DocumentRevision revision = documentCommonService.getRevisionByDocumentAndVersion(document, version);

        Document documentFromRevision = documentCommonService.updateDocumentToRevision(document, revision);

        return documentCommonService.mapDocumentToDocumentDto(documentFromRevision);
    }

    @Transactional
    public DocumentDTO switchToRevision(UUID documentId, UUID revisionId) {
        Document document = documentCommonService.getDocument(documentId);
        DocumentRevision revision = documentCommonService.getRevisionByDocumentAndId(document, revisionId);

        Document documentFromRevision = documentCommonService.updateDocumentToRevision(document, revision);

        return documentCommonService.mapDocumentToDocumentDto(documentFromRevision);
    }

    @Transactional
    public void deleteDocumentWithRevisions(UUID documentId) {
        Document document = documentCommonService.getDocument(documentId);

        List<DocumentRevision> documentRevisions = document.getRevisions();
        documentRevisions.forEach(revision -> documentCommonService.deleteBlobIfDuplicateHashNotExists(revision.getHash()));

        documentCommonService.deleteBlobIfDuplicateHashNotExists(document.getHash());
        documentRepository.delete(document);
    }

    public ResponseEntity<Resource> downloadDocument(UUID documentId) {
        Document document = documentCommonService.getDocument(documentId);
        String hash = document.getHash();
        byte[] data = documentCommonService.getBlob(hash);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(document.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                             .body(new ByteArrayResource(data));
    }

    public Page<DocumentDTO> getDocuments(int pageNumber, int pageSize, String sort, String filter) {
        List<Sort.Order> orders = documentCommonService.getOrdersFromDocumentSort(sort);
        List<FilterItem> filterItems = documentCommonService.getDocumentFilterItemsFromFilter(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(orders));
        Specification<Document> specification = DocumentFilterSpecification.filterByItems(filterItems);

        Page<Document> documents = documentRepository.findAll(specification, pageable);
        List<DocumentDTO> documentDTOs = documents.stream()
                                                  .map(documentCommonService::mapDocumentToDocumentDto)
                                                  .toList();

        return new PageImpl<>(documentDTOs, pageable, documents.getTotalElements());
    }

    public Page<DocumentRevisionDTO> getDocumentRevisions(UUID documentId, int pageNumber, int pageSize, String sort, String filter) {
        Document document = documentCommonService.getDocument(documentId);

        List<Sort.Order> orders = documentCommonService.getOrdersFromRevisionSort(sort);
        List<FilterItem> filterItems = documentCommonService.getRevisionFilterItemsFromFilter(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(orders));
        Specification<DocumentRevision> specification = DocumentFilterSpecification.filterByDocumentAndFilterItems(document, filterItems);

        Page<DocumentRevision> revisions = documentCommonService.getRevisionsBySpecification(specification, pageable);
        List<DocumentRevisionDTO> revisionDTOs = revisions.stream()
                                                          .map(documentCommonService::mapRevisionToRevisionDto)
                                                          .toList();

        return new PageImpl<>(revisionDTOs, pageable, revisions.getTotalElements());
    }

    public Page<Long> getDocumentVersions(UUID documentId, int pageNumber, int pageSize) {
        Document document = documentCommonService.getDocument(documentId);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return documentCommonService.getDocumentVersions(document, pageable);
    }

    @Transactional
    public DocumentDTO moveDocument(UUID documentId, String path) {
        Document document = documentCommonService.getDocument(documentId);

        validateUniquePath(path, document);

        document.setPath(path);
        Document savedDocument = documentRepository.save(document);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return documentCommonService.mapDocumentToDocumentDto(savedDocument);
    }

}
