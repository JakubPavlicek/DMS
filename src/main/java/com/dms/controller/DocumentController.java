package com.dms.controller;

import com.dms.DocumentsApi;
import com.dms.dto.DestinationDTO;
import com.dms.dto.DocumentDTO;
import com.dms.dto.PageWithDocumentsDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.mapper.dto.DocumentDTOMapper;
import com.dms.mapper.dto.PageWithDocumentsDTOMapper;
import com.dms.mapper.dto.PageWithRevisionsDTOMapper;
import com.dms.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class DocumentController implements DocumentsApi {

    private final DocumentService documentService;

    @Override
    public ResponseEntity<Void> archiveDocument(String documentId) {
        documentService.archiveDocument(documentId);
        return ResponseEntity.noContent().build();
    }

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
        Document document = documentService.getDocument(documentId);
        DocumentDTO documentDTO = DocumentDTOMapper.map(document);

        return ResponseEntity.ok(documentDTO);
    }

    @Override
    public ResponseEntity<PageWithRevisionsDTO> getDocumentRevisions(String documentId, Integer page, Integer limit, String sort, String filter) {
        Page<DocumentRevision> revisions = documentService.getDocumentRevisions(documentId, page, limit, sort, filter);
        PageWithRevisionsDTO pageWithRevisionsDTO = PageWithRevisionsDTOMapper.map(revisions);

        return ResponseEntity.ok(pageWithRevisionsDTO);
    }

    @Override
    public ResponseEntity<PageWithDocumentsDTO> getDocuments(Integer page, Integer limit, String sort, String filter) {
        Page<Document> documents = documentService.getDocuments(page, limit, sort, filter);
        PageWithDocumentsDTO pageWithDocumentsDTO = PageWithDocumentsDTOMapper.map(documents);

        return ResponseEntity.ok(pageWithDocumentsDTO);
    }

    @Override
    public ResponseEntity<DocumentDTO> moveDocument(String documentId, DestinationDTO destination) {
        Document document = documentService.moveDocument(documentId, destination.getPath());
        DocumentDTO documentDTO = DocumentDTOMapper.map(document);

        return ResponseEntity.ok(documentDTO);
    }

    @Override
    public ResponseEntity<DocumentDTO> restoreDocument(String documentId) {
        Document document = documentService.restoreDocument(documentId);
        DocumentDTO documentDTO = DocumentDTOMapper.map(document);

        return ResponseEntity.ok(documentDTO);
    }

    @Override
    public ResponseEntity<DocumentDTO> switchToRevision(String documentId, String revisionId) {
        Document document = documentService.switchToRevision(documentId, revisionId);
        DocumentDTO documentDTO = DocumentDTOMapper.map(document);

        return ResponseEntity.ok(documentDTO);
    }

    @Override
    public ResponseEntity<DocumentDTO> uploadDocument(MultipartFile file, DestinationDTO destination) {
        Document document = documentService.uploadDocument(file, destination.getPath());
        DocumentDTO documentDTO = DocumentDTOMapper.map(document);

        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
    }

    @Override
    public ResponseEntity<DocumentDTO> uploadNewDocumentVersion(String documentId, MultipartFile file, DestinationDTO destination) {
        // destination is optional parameter
        String path = destination == null ? null : destination.getPath();

        Document document = documentService.uploadNewDocumentVersion(documentId, file, path);
        DocumentDTO documentDTO = DocumentDTOMapper.map(document);

        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
    }

}
