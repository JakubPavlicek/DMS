package com.dms.controller;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.service.DocumentRevisionService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/revisions")
@AllArgsConstructor
public class DocumentRevisionController {

    private final DocumentRevisionService revisionService;

    @GetMapping
    public List<DocumentRevisionDTO> getRevisions() {
        return revisionService.getRevisions();
    }

    @GetMapping("/{id}")
    public DocumentRevisionDTO getRevision(@PathVariable("id") Long revisionId) {
        return revisionService.getRevision(revisionId);
    }

    @DeleteMapping("/{id}")
    public String deleteRevision(@PathVariable("id") Long revisionId) {
        return revisionService.deleteRevision(revisionId);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadRevision(@PathVariable("id") Long revisionId) {
        return revisionService.downloadRevision(revisionId);
    }

}
