package com.dms.controller;

import com.dms.RevisionsApi;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.entity.DocumentRevision;
import com.dms.mapper.dto.DocumentRevisionDTOMapper;
import com.dms.mapper.dto.PageWithRevisionsDTOMapper;
import com.dms.service.DocumentRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling document {@code /revisions} endpoints.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
public class DocumentRevisionController implements RevisionsApi {

    /** Service responsible for document revision operations. */
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
        DocumentRevision revision = revisionService.getRevision(revisionId);
        DocumentRevisionDTO revisionDTO = DocumentRevisionDTOMapper.map(revision);

        return ResponseEntity.ok(revisionDTO);
    }

    @Override
    public ResponseEntity<PageWithRevisionsDTO> getRevisions(Integer page, Integer limit, String sort, String filter) {
        Page<DocumentRevision> revisions = revisionService.getRevisions(page, limit, sort, filter);
        PageWithRevisionsDTO pageWithRevisionsDTO = PageWithRevisionsDTOMapper.map(revisions);

        return ResponseEntity.ok(pageWithRevisionsDTO);
    }

}
