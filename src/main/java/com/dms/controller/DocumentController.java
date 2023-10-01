package com.dms.controller;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.request.DocumentDestinationRequest;
import com.dms.request.DocumentRequest;
import com.dms.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

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
    public String deleteDocument(@PathVariable("id") String documentId) {
        return documentService.deleteDocument(documentId);
    }

    @PutMapping("/{id}/revisions/{revision}")
    public DocumentRevision switchToRevision(@PathVariable("id") String documentId, @PathVariable("revision") Long revisionId) {
        return documentService.switchToRevision(documentId, revisionId);
    }

    @GetMapping("/{id}/revisions")
    public List<DocumentRevision> getRevisions(@PathVariable("id") String documentId) {
        return documentService.getRevisions(documentId);
    }

    @DeleteMapping("/{id}/revisions/{revision}")
    public String deleteRevision(@PathVariable("id") String documentId, @PathVariable("revision") Long revisionId) {
        return documentService.deleteRevision(documentId, revisionId);
    }

    @PutMapping("/{id}/move")
    public String moveDocument(@PathVariable("id") String documentId, @Valid @RequestBody DocumentDestinationRequest destination) {
        return documentService.moveDocument(documentId, destination);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("id") String documentId) {
        return documentService.downloadDocument(documentId);
    }

    @PostMapping("/{id}/copy")
    public String copyDocument(@PathVariable("id") String documentId, @RequestBody DocumentDestinationRequest destination)
    {
        return documentService.copyDocument(documentId, destination);
    }
}
