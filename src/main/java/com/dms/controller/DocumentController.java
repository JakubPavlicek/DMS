package com.dms.controller;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.request.DocumentRequest;
import com.dms.service.DocumentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/documents")
@AllArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public Document saveDocument(@Valid @ModelAttribute DocumentRequest documentRequest) {
        return documentService.saveDocument(documentRequest);
    }

    @GetMapping("/{id}")
    public Document getDocument(@PathVariable("id") String documentId) {
        return documentService.getDocument(documentId);
    }

    @PutMapping("/{id}")
    public String updateDocument(@PathVariable("id") String documentId, @Valid @ModelAttribute DocumentRequest documentRequest) {
        return documentService.updateDocument(documentId, documentRequest);
    }

    @DeleteMapping("/{id}")
    public String deleteDocumentWithRevisions(@PathVariable("id") String documentId) {
        return documentService.deleteDocumentWithRevisions(documentId);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("id") String documentId) {
        return documentService.downloadDocument(documentId);
    }

    @GetMapping("/{id}/revisions")
    public List<DocumentRevision> getDocumentRevisions(@PathVariable("id") String documentId) {
        return documentService.getRevisions(documentId);
    }

    @PutMapping("/{id}")
    public DocumentRevision switchToVersion(@PathVariable("id") String documentId, @RequestParam("version") Long version) {
        return documentService.switchToVersion(documentId, version);
    }
}
