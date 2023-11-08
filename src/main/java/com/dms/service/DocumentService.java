package com.dms.service;

import com.dms.dto.DestinationDTO;
import com.dms.dto.DocumentDTO;
import com.dms.dto.PageWithDocumentsDTO;
import com.dms.dto.PageWithRevisionsDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Log4j2
@RequiredArgsConstructor
public class DocumentService {

    private final RetrievalService retrievalService;
    private final DownloadService downloadService;
    private final DeletionService deletionService;
    private final UploadService uploadService;
    private final ManipulationService manipulationService;

    public DocumentDTO getDocument(String documentId) {
        return retrievalService.getDocument(documentId);
    }

    @Transactional
    public DocumentDTO uploadDocument(MultipartFile file, DestinationDTO destination) {
        return uploadService.uploadDocument(file, destination);
    }

    @Transactional
    public DocumentDTO uploadNewDocumentVersion(String documentId, MultipartFile file, DestinationDTO destination) {
        return uploadService.uploadNewDocumentVersion(documentId, file, destination);
    }

    @Transactional
    public DocumentDTO switchToRevision(String documentId, String revisionId) {
        return manipulationService.switchToRevision(documentId, revisionId);
    }

    @Transactional
    public void deleteDocumentWithRevisions(String documentId) {
        deletionService.deleteDocumentWithRevisions(documentId);
    }

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        return downloadService.downloadDocument(documentId);
    }

    public PageWithDocumentsDTO getDocuments(int pageNumber, int pageSize, String sort, String filter) {
        return retrievalService.getDocuments(pageNumber, pageSize, sort, filter);
    }

    public PageWithRevisionsDTO getDocumentRevisions(String documentId, int pageNumber, int pageSize, String sort, String filter) {
        return retrievalService.getDocumentRevisions(documentId, pageNumber, pageSize, sort, filter);
    }

    @Transactional
    public DocumentDTO moveDocument(String documentId, DestinationDTO destination) {
        return manipulationService.moveDocument(documentId, destination);
    }

}
