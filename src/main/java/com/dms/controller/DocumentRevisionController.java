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
        @RequestParam(name = "page", defaultValue = "0") @Min(0) int pageNumber,
        @RequestParam(name = "limit", defaultValue = "10") @Min(1) int pageSize,
        @RequestParam(name = "sort", required = false) String sort,
        @RequestParam(name = "filter", required = false) String filter
    ) {
        return revisionService.getRevisions(pageNumber, pageSize, sort, filter);
    }

    @GetMapping("/{id}")
    public DocumentRevisionDTO getRevision(
        @Min(value = 1, message = "Revision ID must be greater than or equal to 1.")
        @PathVariable("id")
        Long revisionId
    ) {
        return revisionService.getRevision(revisionId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRevision(
        @Min(value = 1, message = "Revision ID must be greater than or equal to 1.")
        @PathVariable("id")
        Long revisionId
    ) {
        revisionService.deleteRevision(revisionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadRevision(
        @Min(value = 1, message = "Revision ID must be greater than or equal to 1.")
        @PathVariable("id")
        Long revisionId
    ) {
        return revisionService.downloadRevision(revisionId);
    }

}
