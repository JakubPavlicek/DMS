package com.dms.controller;

import com.dms.DocumentsApi;
import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentWithVersionDTO;
import com.dms.dto.MoveDocumentRequest;
import com.dms.dto.PageWithDocuments;
import com.dms.dto.PageWithRevisions;
import com.dms.dto.PageWithVersions;
import com.dms.dto.UserRequest;
import com.dms.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Validated
public class DocumentController implements DocumentsApi {

    private final DocumentService documentService;

    @Override
    public ResponseEntity<Void> deleteDocumentWithRevisions(UUID id) {
        documentService.deleteDocumentWithRevisions(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> downloadDocument(UUID id) {
        return documentService.downloadDocument(id);
    }

    @Override
    public ResponseEntity<DocumentDTO> getDocument(UUID id) {
        return ResponseEntity.ok(documentService.getDocument(id));
    }

    @Override
    public ResponseEntity<PageWithRevisions> getDocumentRevisions(UUID id, Integer page, Integer limit, String sort, String filter) {
//        return ResponseEntity.ok(documentService.getDocumentRevisions(id, page, limit, sort, filter));
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<PageWithVersions> getDocumentVersions(UUID id, Integer page, Integer limit) {
        return DocumentsApi.super.getDocumentVersions(id, page, limit);
    }

    @Override
    public ResponseEntity<DocumentWithVersionDTO> getDocumentWithVersion(UUID id, Long version) {
        return DocumentsApi.super.getDocumentWithVersion(id, version);
    }

    @Override
    public ResponseEntity<PageWithDocuments> getDocuments(Integer page, Integer limit, String sort, String filter) {
        return DocumentsApi.super.getDocuments(page, limit, sort, filter);
    }

    @Override
    public ResponseEntity<DocumentDTO> moveDocument(UUID id, MoveDocumentRequest moveDocumentRequest) {
        return DocumentsApi.super.moveDocument(id, moveDocumentRequest);
    }

    @Override
    public ResponseEntity<DocumentDTO> switchToRevision(Long id) {
        return DocumentsApi.super.switchToRevision(id);
    }

    @Override
    public ResponseEntity<DocumentDTO> switchToVersion(UUID id, Long version) {
        return DocumentsApi.super.switchToVersion(id, version);
    }

    @Override
    public ResponseEntity<DocumentDTO> uploadDocument(UserRequest user, MultipartFile file, String path) {
        return DocumentsApi.super.uploadDocument(user, file, path);
    }

    @Override
    public ResponseEntity<DocumentDTO> uploadNewDocumentVersion(UUID id, UserRequest user, MultipartFile file, String path) {
        return DocumentsApi.super.uploadNewDocumentVersion(id, user, file, path);
    }

    //    @GetMapping
//    public Page<DocumentDTO> getDocuments(
//        @Min(0)
//        @RequestParam(name = "page", defaultValue = "0") int pageNumber,
//
//        @Min(1)
//        @RequestParam(name = "limit", defaultValue = "10") int pageSize,
//
//        @RequestParam(name = "sort", defaultValue = "name:asc") String sort,
//
//        @RequestParam(name = "filter", defaultValue = "name:") String filter
//    ) {
//        return documentService.getDocuments(pageNumber, pageSize, sort, filter);
//    }
//
//    @PostMapping("/upload")
//    public ResponseEntity<DocumentDTO> uploadDocument(
//        @Valid
//        @RequestPart("user") UserRequest user,
//
//        @ValidFile
//        @RequestPart("file") MultipartFile file,
//
//        @Size(min = 1, max = 255, message = "Length of path must be between 1 and 255 characters.")
//        @RequestPart(name = "path", required = false) String path
//    ) {
//        DocumentDTO documentDTO = documentService.uploadDocument(user, file, path);
//        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
//    }
//
//    @GetMapping("/{documentId}")
//    public DocumentDTO getDocument(
//        @NotNull(message = "Document ID is mandatory.")
//        @PathVariable("documentId") UUID documentId
//    ) {
//        return documentService.getDocument(documentId);
//    }
//
//    @PutMapping("/{documentId}")
//    public ResponseEntity<DocumentDTO> uploadNewDocumentVersion(
//        @NotNull(message = "Document ID is mandatory.")
//        @PathVariable("documentId") UUID documentId,
//
//        @Valid
//        @RequestPart("user") UserRequest user,
//
//        @ValidFile
//        @RequestPart("file") MultipartFile file,
//
//        @Size(min = 1, max = 255, message = "Length of path must be between 1 and 255 characters.")
//        @RequestPart(name = "path", required = false) String path
//    ) {
//        DocumentDTO documentDTO = documentService.uploadNewDocumentVersion(documentId, user, file, path);
//        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
//    }
//
//    @DeleteMapping("/{documentId}")
//    public ResponseEntity<Void> deleteDocumentWithRevisions(
//        @NotNull(message = "Document ID is mandatory.")
//        @PathVariable("documentId") UUID documentId
//    ) {
//        documentService.deleteDocumentWithRevisions(documentId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PutMapping("/{documentId}/move")
//    public DocumentDTO moveDocument(
//        @NotNull(message = "Document ID is mandatory.")
//        @PathVariable("documentId") UUID documentId,
//
//        @NotBlank(message = "Path is mandatory.")
//        @Size(min = 1, max = 255, message = "Length of path must be between 1 and 255 characters.")
//        @RequestPart("path") String path
//    ) {
//        return documentService.moveDocument(documentId, path);
//    }
//
//    @GetMapping("/{documentId}/download")
//    public ResponseEntity<Resource> downloadDocument(
//        @NotNull(message = "Document ID is mandatory.")
//        @PathVariable("documentId") UUID documentId
//    ) {
//        return documentService.downloadDocument(documentId);
//    }
//
//    @GetMapping("/{documentId}/revisions")
//    public Page<DocumentRevisionDTO> getDocumentRevisions(
//        @NotNull(message = "Document ID is mandatory.")
//        @PathVariable("documentId") UUID documentId,
//
//        @Min(0)
//        @RequestParam(name = "page", defaultValue = "0") int pageNumber,
//
//        @Min(1)
//        @RequestParam(name = "limit", defaultValue = "10") int pageSize,
//
//        @RequestParam(name = "sort", defaultValue = "name:asc") String sort,
//
//        @RequestParam(name = "filter", defaultValue = "name:") String filter
//    ) {
//        return documentService.getDocumentRevisions(documentId, pageNumber, pageSize, sort, filter);
//    }
//
//    @PutMapping("/{documentId}/revisions/{revisionId}")
//    public DocumentDTO switchToRevision(
//        @NotNull(message = "Document ID is mandatory.")
//        @PathVariable("documentId") UUID documentId,
//
//        @NotNull(message = "Revision ID is mandatory.")
//        @PathVariable("revisionId") UUID revisionId
//    ) {
//        return documentService.switchToRevision(documentId, revisionId);
//    }
//
//    @GetMapping("/{documentId}/versions")
//    public Page<Long> getDocumentVersions(
//        @NotNull(message = "Document ID is mandatory.")
//        @PathVariable("documentId") UUID documentId,
//
//        @Min(0)
//        @RequestParam(name = "page", defaultValue = "0") int pageNumber,
//
//        @Min(1)
//        @RequestParam(name = "limit", defaultValue = "10") int pageSize
//    ) {
//        return documentService.getDocumentVersions(documentId, pageNumber, pageSize);
//    }
//
//    @GetMapping("/{documentId}/versions/{version}")
//    public DocumentWithVersionDTO getDocumentWithVersion(
//        @NotNull(message = "Document ID is mandatory.")
//        @PathVariable("documentId") UUID documentId,
//
//        @Min(value = 1, message = "Version must be greater than or equal to 1.")
//        @PathVariable("version") Long version
//    ) {
//        return documentService.getDocumentWithVersion(documentId, version);
//    }
//
//    @PutMapping("/{documentId}/versions/{version}")
//    public DocumentDTO switchToVersion(
//        @NotNull(message = "Document ID is mandatory.")
//        @PathVariable("documentId") UUID documentId,
//
//        @Min(value = 1, message = "Version must be greater than or equal to 1.")
//        @PathVariable("version") Long version
//    ) {
//        return documentService.switchToVersion(documentId, version);
//    }

}
