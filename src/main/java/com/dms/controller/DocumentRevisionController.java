package com.dms.controller;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.SortFieldItem;
import com.dms.service.DocumentRevisionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/revisions")
@AllArgsConstructor
@Validated
public class DocumentRevisionController {

    private final DocumentRevisionService revisionService;

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

    @GetMapping
    public Page<DocumentRevisionDTO> getDocumentRevisionsWithPagingAndSorting(
        @RequestParam("page") int pageNumber,
        @RequestParam("size") int pageSize,
        @Valid @RequestParam(name = "sort", required = false) List<SortFieldItem> sortFieldItems
    ) {
        return revisionService.getDocumentRevisionsWithPagingAndSorting(pageNumber, pageSize, sortFieldItems);
    }

}
