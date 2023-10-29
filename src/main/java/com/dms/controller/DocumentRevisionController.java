package com.dms.controller;

import com.dms.RevisionsApi;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithRevisions;
import com.dms.service.DocumentRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
public class DocumentRevisionController implements RevisionsApi {

    private final DocumentRevisionService revisionService;

    @Override
    public ResponseEntity<Void> deleteRevision(UUID revisionId) {
        revisionService.deleteRevision(revisionId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> downloadRevision(UUID revisionId) {
        return revisionService.downloadRevision(revisionId);
    }

    @Override
    public ResponseEntity<DocumentRevisionDTO> getRevision(UUID revisionId) {
        return ResponseEntity.ok(revisionService.getRevision(revisionId));
    }

    @Override
    public ResponseEntity<PageWithRevisions> getRevisions(Integer page, Integer limit, String sort, String filter) {
        return ResponseEntity.ok(revisionService.getRevisions(page, limit, sort, filter));
    }

    //    @GetMapping
//    public Page<DocumentRevisionDTO> getRevisions(
//        @Min(0)
//        @RequestParam(name = "page", defaultValue = "0") int pageNumber,
//
//        @Min(1)
//        @RequestParam(name = "limit", defaultValue = "10") int pageSize,
//
//        @RequestParam(name = "sort", defaultValue = "name:asc") String sort,
//
//        @RequestParam(name = "filter", defaultValue = "name:") String filter
//    ) {
//        return revisionService.getRevisions(pageNumber, pageSize, sort, filter);
//    }
//
//    @GetMapping("/{revisionId}")
//    public DocumentRevisionDTO getRevision(
//        @NotNull(message = "Revision ID is mandatory.")
//        @PathVariable("revisionId") UUID revisionId
//    ) {
//        return revisionService.getRevision(revisionId);
//    }
//
//    @DeleteMapping("/{revisionId}")
//    public ResponseEntity<Void> deleteRevision(
//        @NotNull(message = "Revision ID is mandatory.")
//        @PathVariable("revisionId") UUID revisionId
//    ) {
//        revisionService.deleteRevision(revisionId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/{revisionId}/download")
//    public ResponseEntity<Resource> downloadRevision(
//        @NotNull(message = "Revision ID is mandatory.")
//        @PathVariable("revisionId") UUID revisionId
//    ) {
//        return revisionService.downloadRevision(revisionId);
//    }

}
