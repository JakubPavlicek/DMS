package com.dms.controller;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.UserDTO;
import com.dms.service.DocumentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
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
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public DocumentDTO saveDocument(@Valid @RequestPart("user") UserDTO user, @RequestPart("file") MultipartFile file) {
        return documentService.saveDocument(user, file);
    }

    @GetMapping("/{id}")
    public DocumentDTO getDocument(@PathVariable("id") String documentId) {
        return documentService.getDocumentDTO(documentId);
    }

    @PutMapping("/{id}")
    public String updateDocument(@PathVariable("id") String documentId, @Valid @RequestPart("user") UserDTO user, @RequestPart("file") MultipartFile file) {
        return documentService.updateDocument(documentId, user, file);
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
    public List<DocumentRevisionDTO> getDocumentRevisions(@PathVariable("id") String documentId) {
        return documentService.getDocumentRevisions(documentId);
    }

    @PutMapping("/{id}/versions")
    public DocumentRevisionDTO switchToVersion(@PathVariable("id") String documentId, @RequestParam("version") Long version) {
        return documentService.switchToVersion(documentId, version);
    }

}
