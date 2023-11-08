package com.dms.service;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithRevisionsDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class DocumentRevisionService {

    private final RetrievalService retrievalService;
    private final DownloadService downloadService;
    private final DeletionService deletionService;

    public DocumentRevisionDTO getRevision(String revisionId) {
        return retrievalService.getRevision(revisionId);
    }

    @Transactional
    public void deleteRevision(String revisionId) {
        deletionService.deleteRevision(revisionId);
    }

    public ResponseEntity<Resource> downloadRevision(String revisionId) {
        return downloadService.downloadRevision(revisionId);
    }

    public PageWithRevisionsDTO getRevisions(int pageNumber, int pageSize, String sort, String filter) {
        return retrievalService.getRevisions(pageNumber, pageSize, sort, filter);
    }

}
