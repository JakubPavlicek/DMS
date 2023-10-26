package com.dms.controller;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.service.DocumentRevisionService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/revisions")
@RequiredArgsConstructor
@Validated
public class DocumentRevisionController {

    private final DocumentRevisionService revisionService;

    @GetMapping
    public Page<DocumentRevisionDTO> getRevisions(
        @Min(0)
        @RequestParam(name = "page", defaultValue = "0") int pageNumber,

        @Min(1)
        @RequestParam(name = "limit", defaultValue = "10") int pageSize,

        @RequestParam(name = "sort", defaultValue = "name:asc") String sort,

        @RequestParam(name = "filter", defaultValue = "name:") String filter
    ) {
        return revisionService.getRevisions(pageNumber, pageSize, sort, filter);
    }

    @GetMapping("/{revisionId}")
    public DocumentRevisionDTO getRevision(
        @Min(value = 1, message = "Revision ID must be greater than or equal to 1.")
        @PathVariable("revisionId") Long revisionId
    ) {
        return revisionService.getRevision(revisionId);
    }

    @DeleteMapping("/{revisionId}")
    public ResponseEntity<Void> deleteRevision(
        @Min(value = 1, message = "Revision ID must be greater than or equal to 1.")
        @PathVariable("revisionId") Long revisionId
    ) {
        revisionService.deleteRevision(revisionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{revisionId}/download")
    public ResponseEntity<Resource> downloadRevision(
        @Min(value = 1, message = "Revision ID must be greater than or equal to 1.")
        @PathVariable("revisionId") Long revisionId
    ) {
        return revisionService.downloadRevision(revisionId);
    }

}
