package com.dms.service;

import com.dms.dto.DestinationDTO;
import com.dms.dto.DocumentDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.mapper.dto.DocumentDTOMapper;
import com.dms.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ManipulationService {

    private final DocumentRepository documentRepository;

    private final ManagementService managementService;

    @Transactional
    public DocumentDTO switchToRevision(String documentId, String revisionId) {
        log.debug("Request - Switching document to revision: documentId={}, revision={}", documentId, revisionId);

        Document document = managementService.getDocument(documentId);
        DocumentRevision revision = managementService.getRevisionByDocumentAndId(document, revisionId);

        Document documentFromRevision = managementService.updateDocumentToRevision(document, revision);

        log.info("Successfully switched document {} to revision {}", documentId, revisionId);

        return DocumentDTOMapper.map(documentFromRevision);
    }

    @Transactional
    public DocumentDTO moveDocument(String documentId, DestinationDTO destination) {
        log.debug("Request - Moving document: documentId={}, destination={}", documentId, destination);

        Document document = managementService.getDocument(documentId);
        String path = destination.getPath();

        managementService.validateUniquePath(path, document);

        document.setPath(path);
        Document savedDocument = documentRepository.save(document);

        managementService.saveRevisionFromDocument(savedDocument);

        log.info("Document {} moved successfully to path {}", documentId, path);

        return DocumentDTOMapper.map(savedDocument);
    }

}
