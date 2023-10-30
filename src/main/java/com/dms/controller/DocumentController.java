package com.dms.controller;

import com.dms.DocumentsApi;
import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentWithVersionDTO;
import com.dms.dto.PageWithDocuments;
import com.dms.dto.PageWithRevisions;
import com.dms.dto.PageWithVersions;
import com.dms.dto.UserRequest;
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
    public ResponseEntity<PageWithRevisions> getDocumentRevisions(String documentId, Integer page, Integer limit, String sort, String filter) {
        return ResponseEntity.ok(documentService.getDocumentRevisions(documentId, page, limit, sort, filter));
    }

    @Override
    public ResponseEntity<PageWithVersions> getDocumentVersions(String documentId, Integer page, Integer limit) {
        return ResponseEntity.ok(documentService.getDocumentVersions(documentId, page, limit));
    }

    @Override
    public ResponseEntity<DocumentWithVersionDTO> getDocumentWithVersion(String documentId, Long version) {
        return ResponseEntity.ok(documentService.getDocumentWithVersion(documentId, version));
    }

    @Override
    public ResponseEntity<PageWithDocuments> getDocuments(Integer page, Integer limit, String sort, String filter) {
        return ResponseEntity.ok(documentService.getDocuments(page, limit, sort, filter));
    }

    @Override
    public ResponseEntity<DocumentDTO> moveDocument(String documentId, String path) {
        return ResponseEntity.ok(documentService.moveDocument(documentId, path));
    }

    @Override
    public ResponseEntity<DocumentDTO> switchToRevision(String documentId, String revisionId) {
        return ResponseEntity.ok(documentService.switchToRevision(documentId, revisionId));
    }

    @Override
    public ResponseEntity<DocumentDTO> switchToVersion(String documentId, Long version) {
        return ResponseEntity.ok(documentService.switchToVersion(documentId, version));
    }

    @Override
    public ResponseEntity<DocumentDTO> uploadDocument(UserRequest user, MultipartFile file, String path) {
        DocumentDTO documentDTO = documentService.uploadDocument(user, file, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
    }

    @Override
    public ResponseEntity<DocumentDTO> uploadNewDocumentVersion(String documentId, UserRequest user, MultipartFile file, String path) {
        DocumentDTO documentDTO = documentService.uploadNewDocumentVersion(documentId, user, file, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
    }

}
