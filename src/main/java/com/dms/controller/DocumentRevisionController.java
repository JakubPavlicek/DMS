package com.dms.controller;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.service.DocumentRevisionService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

import java.util.UUID;

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
        @NotNull(message = "Revision ID is mandatory.")
        @PathVariable("revisionId") UUID revisionId
    ) {
        return revisionService.getRevision(revisionId);
    }

    @DeleteMapping("/{revisionId}")
    public ResponseEntity<Void> deleteRevision(
        @NotNull(message = "Revision ID is mandatory.")
        @PathVariable("revisionId") UUID revisionId
    ) {
        revisionService.deleteRevision(revisionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{revisionId}/download")
    public ResponseEntity<Resource> downloadRevision(
        @NotNull(message = "Revision ID is mandatory.")
        @PathVariable("revisionId") UUID revisionId
    ) {
        return revisionService.downloadRevision(revisionId);
    }

}
