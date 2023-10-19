package com.dms.service;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.FilterItem;
import com.dms.dto.SortItem;
import com.dms.dto.UserRequest;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
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

    public DocumentDTO getDocument(UUID documentId, Long version) {
        if (Objects.isNull(version))
            return getDocument(documentId);

        Document document = documentCommonService.getDocument(documentId, version);

        return documentCommonService.mapDocumentToDocumentDto(document);
    }

    private LocalDateTime getDocumentCreatedAt(UUID documentId) {
        return documentRepository.getCreatedAtByDocumentId(documentId)
                                 .orElseThrow(() -> new RuntimeException("Nebyl nalezen cas vytvoreni dokumentu s ID: " + documentId));
    }

    public Page<DocumentRevisionDTO> getDocumentRevisions(UUID documentId, int pageNumber, int pageSize, String sort, String filter) {
        Document document = documentCommonService.getDocument(documentId);

        List<SortItem> sortItems = documentCommonService.parseSortItems(sort);
        List<FilterItem> filterItems = documentCommonService.parseFilterItems(filter);

        Pageable pageable = documentCommonService.createPageable(pageNumber, pageSize, sortItems);

        Page<DocumentRevision> revisions = getFilteredDocumentRevisions(filterItems, document, pageable);
        List<DocumentRevisionDTO> revisionDTOs = revisions.stream()
                                                          .map(documentCommonService::mapRevisionToRevisionDto)
                                                          .toList();

        return new PageImpl<>(revisionDTOs, pageable, revisions.getTotalElements());
    }

    private Page<DocumentRevision> getFilteredDocumentRevisions(List<FilterItem> filterItems, Document document, Pageable pageable) {
        if (Objects.isNull(filterItems))
            return documentCommonService.getRevisionsByDocument(document, pageable);

        Specification<DocumentRevision> specification = DocumentFilterSpecification.filterByDocumentAndFilterItems(document, filterItems);
        return documentCommonService.getRevisionsBySpecification(specification, pageable);
    }

    private User getUserFromUserRequest(UserRequest userRequest) {
        User user = User.builder()
                        .username(userRequest.getUsername())
                        .email(userRequest.getEmail())
                        .build();

        return userService.getUser(user);
    }

    private Document createDocumentFromUserRequestAndFile(UserRequest userRequest, MultipartFile file, String path) {
        String hash = documentCommonService.storeBlob(file);
        User author = getUserFromUserRequest(userRequest);

        return createDocument(file, hash, path, author);
    }

    private Document createDocument(MultipartFile file, String hash, String path, User author) {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String cleanPath = StringUtils.cleanPath(originalFileName);
        String name = StringUtils.getFilename(cleanPath);
        String type = file.getContentType();

        if(documentCommonService.pathWithFileAlreadyExists(path, name, author))
            throw new RuntimeException("Soubor: " + name + " se jiz v ceste: " + path + " vyskytuje");

        return Document.builder()
                       .name(name)
                       .type(type)
                       .path(path)
                       .hash(hash)
                       .author(author)
                       .build();
    }

    @Transactional
    public DocumentDTO uploadDocument(UserRequest userRequest, MultipartFile file, String path) {
        Document document = createDocumentFromUserRequestAndFile(userRequest, file, path);

        // flush to immediately initialize the "createdAt" and "updatedAt" fields, ensuring the DTO does not contain null values for these properties
        Document savedDocument = documentRepository.saveAndFlush(document);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return documentCommonService.mapDocumentToDocumentDto(savedDocument);
    }

    @Transactional
    public DocumentDTO updateDocument(UUID documentId, UserRequest userRequest, MultipartFile file, String path) {
        if (!documentRepository.existsById(documentId))
            throw new DocumentNotFoundException("Nebyl nalezen soubor s ID: " + documentId + " pro nahrazeni");

        Document document = createDocumentFromUserRequestAndFile(userRequest, file, path);
        document.setDocumentId(documentId);

        // flush to immediately initialize the "updatedAt" field, ensuring the DTO does not contain null values for this property
        Document savedDocument = documentRepository.saveAndFlush(document);

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
    public DocumentDTO switchToRevision(UUID documentId, Long revisionId) {
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
        List<SortItem> sortItems = documentCommonService.parseSortItems(sort);
        List<FilterItem> filterItems = documentCommonService.parseFilterItems(filter);

        Pageable pageable = documentCommonService.createPageable(pageNumber, pageSize, sortItems);

        Page<Document> documents = getFilteredDocuments(filterItems, pageable);
        List<DocumentDTO> documentDTOs = documents.stream()
                                                  .map(documentCommonService::mapDocumentToDocumentDto)
                                                  .toList();

        return new PageImpl<>(documentDTOs, pageable, documents.getTotalElements());
    }

    private Page<Document> getFilteredDocuments(List<FilterItem> filterItems, Pageable pageable) {
        if (Objects.isNull(filterItems))
            return documentRepository.findAll(pageable);

        Specification<Document> specification = DocumentFilterSpecification.filterByItems(filterItems);
        return documentRepository.findAll(specification, pageable);
    }

    public Page<Long> getDocumentVersions(UUID documentId, int pageNumber, int pageSize) {
        Document document = documentCommonService.getDocument(documentId);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return documentCommonService.getDocumentVersions(document, pageable);
    }

    @Transactional
    public DocumentRevisionDTO uploadRevision(UUID documentId, UserRequest userRequest, MultipartFile file, String path) {
        Document databaseDocument = documentCommonService.getDocument(documentId);
        Document document = createDocumentFromUserRequestAndFile(userRequest, file, path);

        DocumentRevision revision = DocumentRevision.builder()
                                                    .document(databaseDocument)
                                                    .name(document.getName())
                                                    .type(document.getType())
                                                    .path(document.getPath())
                                                    .author(document.getAuthor())
                                                    .hash(document.getHash())
                                                    .version(documentCommonService.getLastRevisionVersion(databaseDocument))
                                                    .build();

        DocumentRevision savedRevision = documentCommonService.saveRevision(revision);

        return documentCommonService.mapRevisionToRevisionDto(savedRevision);
    }

    public DocumentDTO moveDocument(UUID documentId, String path) {
        Document document = documentCommonService.getDocument(documentId);

        String filename = document.getName();

        // user can't have a duplicate path for a document with the same name
        if(documentCommonService.pathWithFileAlreadyExists(path, filename, document.getAuthor()))
            throw new RuntimeException("Soubor: " + filename + " se jiz v ceste: " + path + " vyskytuje");

        document.setPath(path);

        Document savedDocument = documentRepository.save(document);

        return documentCommonService.mapDocumentToDocumentDto(savedDocument);
    }

}
