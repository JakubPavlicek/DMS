package com.dms.controller;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.UserRequest;
import com.dms.service.DocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @GetMapping
    public Page<DocumentDTO> getDocuments(
        @Min(0)
        @RequestParam(name = "page", defaultValue = "0") int pageNumber,

        @Min(1)
        @RequestParam(name = "limit", defaultValue = "10") int pageSize,

        @RequestParam(name = "sort", required = false) String sort,

        @RequestParam(name = "filter", required = false) String filter
    ) {
        return documentService.getDocuments(pageNumber, pageSize, sort, filter);
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentDTO> uploadDocument(
        @Valid
        @RequestPart("user") UserRequest user,

        @RequestPart("file") MultipartFile file,

        @Size(min = 1, max = 255, message = "Length of path must be between 1 and 255 characters.")
        @RequestPart(name = "path", required = false) String path
    ) {
        DocumentDTO documentDTO = documentService.uploadDocument(user, file, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
    }

    @GetMapping("/{id}")
    public DocumentDTO getDocument(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId
    ) {
        return documentService.getDocument(documentId);
    }

    @PutMapping("/{id}")
    public DocumentDTO updateDocument(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId,

        @Valid
        @RequestPart("user") UserRequest user,

        @RequestPart("file") MultipartFile file,

        @Size(min = 1, max = 255, message = "Length of path must be between 1 and 255 characters.")
        @RequestPart(name = "path", required = false) String path
    ) {
        return documentService.updateDocument(documentId, user, file, path);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentWithRevisions(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId
    ) {
        documentService.deleteDocumentWithRevisions(documentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/move")
    public DocumentDTO moveDocument(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId,

        @NotBlank(message = "Path is mandatory.")
        @Size(min = 1, max = 255, message = "Length of path must be between 1 and 255 characters.")
        @RequestPart("path") String path
    ) {
        return documentService.moveDocument(documentId, path);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId
    ) {
        return documentService.downloadDocument(documentId);
    }

    @GetMapping("/{id}/revisions")
    public Page<DocumentRevisionDTO> getDocumentRevisions(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId,

        @Min(0)
        @RequestParam(name = "page", defaultValue = "0") int pageNumber,

        @Min(1)
        @RequestParam(name = "limit", defaultValue = "10") int pageSize,

        @RequestParam(name = "sort", required = false) String sort,

        @RequestParam(name = "filter", required = false) String filter
    ) {
        return documentService.getDocumentRevisions(documentId, pageNumber, pageSize, sort, filter);
    }

    @PostMapping("/{id}/revisions/upload")
    public ResponseEntity<DocumentRevisionDTO> uploadRevision(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId,

        @Valid
        @RequestPart("user") UserRequest user,

        @RequestPart("file") MultipartFile file,

        @Size(min = 1, max = 255, message = "Length of path must be between 1 and 255 characters.")
        @RequestPart(name = "path", required = false) String path
    ) {
        DocumentRevisionDTO documentRevisionDTO = documentService.uploadRevision(documentId, user, file, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentRevisionDTO);
    }

    @PutMapping("/{id}/revisions/{revision}")
    public DocumentDTO switchToRevision(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId,

        @Min(value = 1, message = "Revision ID must be greater than or equal to 1.")
        @PathVariable("revision") Long revisionId
    ) {
        return documentService.switchToRevision(documentId, revisionId);
    }

    @GetMapping("/{id}/versions")
    public Page<Long> getDocumentVersions(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId,

        @Min(0)
        @RequestParam(name = "page", defaultValue = "0") int pageNumber,

        @Min(1)
        @RequestParam(name = "limit", defaultValue = "10") int pageSize
    ) {
        return documentService.getDocumentVersions(documentId, pageNumber, pageSize);
    }

    @GetMapping("/{id}/versions/{version}")
    public DocumentDTO getDocument(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId,

        @Min(value = 1, message = "Version must be greater than or equal to 1.")
        @PathVariable("version") Long version
    ) {
        return documentService.getDocument(documentId, version);
    }

    @PutMapping("/{id}/versions/{version}")
    public DocumentDTO switchToVersion(
        @NotBlank(message = "Document ID is mandatory.")
        @Size(min = 36, max = 36, message = "Length of document ID must be 36 characters.")
        @PathVariable("id") String documentId,

        @Min(value = 1, message = "Version must be greater than or equal to 1.")
        @PathVariable("version") Long version
    ) {
        return documentService.switchToVersion(documentId, version);
    }

}
