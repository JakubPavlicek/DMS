package com.dms.controller;

import com.dms.entity.DocumentFile;
import com.dms.model.DocumentFileRequest;
import com.dms.service.DocumentFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
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
    public DocumentFile saveDocumentFile(@ModelAttribute DocumentFileRequest fileRequest) {
        return documentFileService.saveDocumentFile(fileRequest);
    }

    @GetMapping("/download/{id}")
    public DocumentFile getDocumentFile(@PathVariable("id") String id) {
        return documentFileService.getDocumentFile(id);
    }

    @PutMapping("/update/{id}")
    public String updateDocumentFile(@PathVariable("id") String id, @RequestBody DocumentFile file) {
        return documentFileService.updateDocumentFile(id, file);
    }

    @PutMapping("/{id}/revisions/{revision}")
    public DocumentFile switchToRevision(@PathVariable("id") String id, @PathVariable("revision") Long revision)
    {
        return documentFileService.switchToRevision(id, revision);
    }

    @GetMapping("/{id}/revisions")
    public List<Revision<Long, DocumentFile>> getRevisions(@PathVariable("id") String id)
    {
        return documentFileService.getRevisions(id);
    }

    @DeleteMapping("/{id}/revisions/{revision}")
    public String deleteRevision(@PathVariable("id") String id, @PathVariable("revision") Long revision)
    {
        return documentFileService.deleteRevision(id, revision);
    }

}
