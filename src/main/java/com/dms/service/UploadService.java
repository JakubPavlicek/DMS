package com.dms.service;

import com.dms.dto.DestinationDTO;
import com.dms.dto.DocumentDTO;
import com.dms.entity.Document;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.mapper.dto.DocumentDTOMapper;
import com.dms.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class UploadService {

    private final DocumentRepository documentRepository;

    private final ManagementService managementService;

    private Document createDocument(MultipartFile file, String path) {
        String hash = managementService.storeBlob(file);
        User author = managementService.getAuthenticatedUser();

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

    @Transactional
    public DocumentDTO uploadDocument(MultipartFile file, DestinationDTO destination) {
        log.debug("Request - Uploading document: file={}, destination={}", file.getOriginalFilename(), destination);

        String path = destination.getPath();
        Document document = createDocument(file, path);

        managementService.validateUniquePath(path, document);

        // flush to immediately initialize the "createdAt" and "updatedAt" fields, ensuring the DTO does not contain null values for these properties
        Document savedDocument = documentRepository.saveAndFlush(document);

        log.info("Document {} with ID {} uploaded successfully", document.getName(), document.getDocumentId());

        managementService.saveRevisionFromDocument(savedDocument);

        return DocumentDTOMapper.map(savedDocument);
    }

    @Transactional
    public DocumentDTO uploadNewDocumentVersion(String documentId, MultipartFile file, DestinationDTO destination) {
        log.debug("Request - Uploading new document version: documentId={}, file={}, destination={}", documentId, file.getOriginalFilename(), destination);

        if (!documentRepository.existsByDocumentId(documentId)) {
            throw new DocumentNotFoundException("File with ID: " + documentId + " not found for replacement");
        }

        Document databaseDocument = managementService.getDocument(documentId);
        String path = destination.getPath();

        Document document = createDocument(file, path);
        document.setId(databaseDocument.getId());
        document.setDocumentId(documentId);
        document.setVersion(managementService.getLastRevisionVersion(document) + 1);

        managementService.validateUniquePath(path, document);

        // flush to immediately initialize the "updatedAt" field, ensuring the DTO does not contain null values for this property
        Document savedDocument = documentRepository.saveAndFlush(document);

        // createdAt column is not initialized because of "updatable = false" -> set it manually
        LocalDateTime createdAt = getDocumentCreatedAt(documentId);
        savedDocument.setCreatedAt(createdAt);

        log.info("Successfully uploaded new document version for document {}", documentId);

        managementService.saveRevisionFromDocument(savedDocument);

        return DocumentDTOMapper.map(savedDocument);
    }

    private LocalDateTime getDocumentCreatedAt(String documentId) {
        return documentRepository.getCreatedAtByDocumentId(documentId)
                                 .orElseThrow(() -> new RuntimeException("Creation time not found for file with ID: " + documentId));
    }

}
