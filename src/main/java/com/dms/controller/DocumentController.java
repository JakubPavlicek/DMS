package com.dms.controller;

import com.dms.DocumentsApi;
import com.dms.dto.DocumentDTO;
import com.dms.dto.PageWithDocumentsDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.dto.PathRequestDTO;
import com.dms.dto.UserRequestDTO;
import com.dms.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class DocumentController implements DocumentsApi {

    private final DocumentService documentService;

    @Override
    public ResponseEntity<Void> deleteDocumentWithRevisions(String documentId) {
        documentService.deleteDocumentWithRevisions(documentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> downloadDocument(String documentId) {
        return documentService.downloadDocument(documentId);
    }

    @Override
    public ResponseEntity<DocumentDTO> getDocument(String documentId) {
        return ResponseEntity.ok(documentService.getDocument(documentId));
    }

    @Override
    public ResponseEntity<PageWithRevisionsDTO> getDocumentRevisions(String documentId, Integer page, Integer limit, String sort, String filter) {
        return ResponseEntity.ok(documentService.getDocumentRevisions(documentId, page, limit, sort, filter));
    }

    @Override
    public ResponseEntity<PageWithDocumentsDTO> getDocuments(Integer page, Integer limit, String sort, String filter) {
        return ResponseEntity.ok(documentService.getDocuments(page, limit, sort, filter));
    }

    @Override
    public ResponseEntity<DocumentDTO> moveDocument(String documentId, PathRequestDTO path) {
        return ResponseEntity.ok(documentService.moveDocument(documentId, path));
    }

    @Override
    public ResponseEntity<DocumentDTO> switchToRevision(String documentId, String revisionId) {
        return ResponseEntity.ok(documentService.switchToRevision(documentId, revisionId));
    }

    @Override
    public ResponseEntity<DocumentDTO> uploadDocument(UserRequestDTO user, MultipartFile file, PathRequestDTO path) {
        DocumentDTO documentDTO = documentService.uploadDocument(user, file, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
    }

    @Override
    public ResponseEntity<DocumentDTO> uploadNewDocumentVersion(String documentId, UserRequestDTO user, MultipartFile file, PathRequestDTO path) {
        DocumentDTO documentDTO = documentService.uploadNewDocumentVersion(documentId, user, file, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
    }

}
