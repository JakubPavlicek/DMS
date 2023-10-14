package com.dms.controller;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.FilterItem;
import com.dms.dto.SortItem;
import com.dms.dto.UserDTO;
import com.dms.service.DocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/documents")
@AllArgsConstructor
@Validated
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentDTO> saveDocument(@Valid @RequestPart("user") UserDTO user, @RequestPart("file") MultipartFile file) {
        DocumentDTO documentDTO = documentService.saveDocument(user, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentDTO);
    }

    @GetMapping("/{id}")
    public DocumentDTO getDocument(@PathVariable("id") String documentId, @RequestParam(value = "version", required = false) @NotNull(message = "Version must not be null") @Min(1) Long version) {
        return documentService.getDocument(documentId, version);
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

    @PutMapping("/{id}/versions")
    public DocumentRevisionDTO switchToVersion(@PathVariable("id") String documentId, @RequestParam("version") Long version) {
        return documentService.switchToVersion(documentId, version);
    }

    @GetMapping("/{id}/revisions")
    public Page<DocumentRevisionDTO> getDocumentRevisions(
        @PathVariable("id") String documentId,
        @RequestParam("page") int pageNumber,
        @RequestParam("limit") int pageSize,
        @Valid @RequestParam(name = "sortItems", required = false) List<SortItem> sortItems,
        @Valid @RequestParam(name = "filter", required = false) List<FilterItem> filterItems
    ) {
        return documentService.getDocumentRevisions(documentId, pageNumber, pageSize, sortItems, filterItems);
    }

    @GetMapping
    public Page<DocumentDTO> getDocuments(
        @RequestParam(value = "page", defaultValue = "0") int pageNumber,
        @RequestParam(value = "limit", defaultValue = "10") int pageSize,
        @Valid @RequestParam(name = "sort", required = false) List<SortItem> sortItems,
        @Valid @RequestParam(name = "filter", required = false) List<FilterItem> filterItems
    ) {
        return documentService.getDocuments(pageNumber, pageSize, sortItems, filterItems);
    }

}
