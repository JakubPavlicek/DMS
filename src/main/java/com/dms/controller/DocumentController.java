package com.dms.controller;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.UserDTO;
import com.dms.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Validated
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentDTO> saveDocument(@Valid @RequestPart("user") UserDTO user, @RequestPart("file") MultipartFile file) {
        DocumentDTO documentDTO = documentService.saveDocument(user, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
    }

    @GetMapping("/{id}")
    public DocumentDTO getDocument(@PathVariable("id") String documentId) {
        return documentService.getDocument(documentId);
    }

    @PutMapping("/{id}")
    public DocumentDTO updateDocument(@PathVariable("id") String documentId, @Valid @RequestPart("user") UserDTO user, @RequestPart("file") MultipartFile file) {
        return documentService.updateDocument(documentId, user, file);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentWithRevisions(@PathVariable("id") String documentId) {
        documentService.deleteDocumentWithRevisions(documentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("id") String documentId) {
        return documentService.downloadDocument(documentId);
    }

    @GetMapping("/{id}/versions/{version}")
    public DocumentDTO getDocument(@PathVariable("id") String documentId, @PathVariable("version") Long version) {
        return documentService.getDocument(documentId, version);
    }

    @PutMapping("/{id}/versions/{version}")
    public DocumentDTO switchToVersion(@PathVariable("id") String documentId, @PathVariable("version") Long version) {
        return documentService.switchToVersion(documentId, version);
    }

    @PutMapping("/{id}/revisions/{revision}")
    public DocumentDTO switchToRevision(@PathVariable("id") String documentId, @PathVariable("revision") Long revisionId) {
        return documentService.switchToRevision(documentId, revisionId);
    }

    @GetMapping("/{id}/revisions")
    public Page<DocumentRevisionDTO> getDocumentRevisions(
        @PathVariable("id") String documentId,
        @RequestParam("page") int pageNumber,
        @RequestParam("limit") int pageSize,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "filter", required = false) String filter
    ) {
        return documentService.getDocumentRevisions(documentId, pageNumber, pageSize, sort, filter);
    }

    @GetMapping
    public Page<DocumentDTO> getDocuments(
        @RequestParam(name = "page", defaultValue = "0") int pageNumber,
        @RequestParam(name = "limit", defaultValue = "10") int pageSize,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "filter", required = false) String filter
    ) {
        return documentService.getDocuments(pageNumber, pageSize, sort, filter);
    }

}
