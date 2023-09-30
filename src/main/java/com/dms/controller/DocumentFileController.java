package com.dms.controller;

import com.dms.entity.DocumentFile;
import com.dms.entity.DocumentFileRevision;
import com.dms.request.DocumentFileRequest;
import com.dms.service.DocumentFileService;
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
@RequestMapping("/files")
public class DocumentFileController {
    private final DocumentFileService documentFileService;

    @Autowired
    public DocumentFileController(DocumentFileService documentFileService) {
        this.documentFileService = documentFileService;
    }

    @PostMapping("/upload")
    public DocumentFile saveDocumentFile(@Valid @ModelAttribute DocumentFileRequest fileRequest) {
        return documentFileService.saveDocumentFile(fileRequest);
    }

    @GetMapping("/{id}")
    public DocumentFile getDocumentFile(@PathVariable("id") String fileId) {
        return documentFileService.getDocumentFile(fileId);
    }

    @PutMapping("/{id}")
    public String updateDocumentFile(@PathVariable("id") String fileId, @Valid @ModelAttribute DocumentFileRequest fileRequest) {
        return documentFileService.updateDocumentFile(fileId, fileRequest);
    }

    @DeleteMapping("/{id}")
    public String deleteDocumentFile(@PathVariable("id") String fileId) {
        return documentFileService.deleteDocumentFile(fileId);
    }

    @PutMapping("/{id}/revisions/{revision}")
    public DocumentFileRevision switchToRevision(@PathVariable("id") String fileId, @PathVariable("revision") Long revisionId) {
        return documentFileService.switchToRevision(fileId, revisionId);
    }

    @GetMapping("/{id}/revisions")
    public List<DocumentFileRevision> getRevisions(@PathVariable("id") String fileId) {
        return documentFileService.getRevisions(fileId);
    }

    @DeleteMapping("/{id}/revisions/{revision}")
    public String deleteRevision(@PathVariable("id") String fileId, @PathVariable("revision") Long revisionId) {
        return documentFileService.deleteRevision(fileId, revisionId);
    }

    @PutMapping("/{id}/move")
    public String moveDocumentFile(@PathVariable("id") String fileId, @RequestBody String path) {
        return documentFileService.moveDocumentFile(fileId, path);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocumentFile(@PathVariable("id") String fileId) {
        return documentFileService.downloadDocumentFile(fileId);
    }
}
