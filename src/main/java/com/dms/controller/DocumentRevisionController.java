package com.dms.controller;

import com.dms.RevisionsApi;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithRevisions;
import com.dms.service.DocumentRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DocumentRevisionController implements RevisionsApi {

    private final DocumentRevisionService revisionService;

    @Override
    public ResponseEntity<Void> deleteRevision(String revisionId) {
        revisionService.deleteRevision(revisionId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> downloadRevision(String revisionId) {
        return revisionService.downloadRevision(revisionId);
    }

    @Override
    public ResponseEntity<DocumentRevisionDTO> getRevision(String revisionId) {
        return ResponseEntity.ok(revisionService.getRevision(revisionId));
    }

    @Override
    public ResponseEntity<PageWithRevisions> getRevisions(Integer page, Integer limit, String sort, String filter) {
        return ResponseEntity.ok(revisionService.getRevisions(page, limit, sort, filter));
    }

}
