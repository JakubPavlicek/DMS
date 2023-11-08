package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Log4j2
public class DownloadService {

    private final ManagementService managementService;

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        log.debug("Request - Downloading document: documentId={}", documentId);

        Document document = managementService.getDocument(documentId);

        String hash = document.getHash();
        Resource file = managementService.getBlob(hash);

        log.info("Document {} downloaded successfully", documentId);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(document.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                             .header(HttpHeaders.CONTENT_LENGTH, getContentLength(file))
                             .body(file);
    }

    public ResponseEntity<Resource> downloadRevision(String revisionId) {
        log.debug("Request - Downloading revision: revisionId={}", revisionId);

        DocumentRevision revision = managementService.getRevision(revisionId);

        String hash = revision.getHash();
        Resource file = managementService.getBlob(hash);

        log.info("Revision {} downloaded successfully", revisionId);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(revision.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + revision.getName() + "\"")
                             .header(HttpHeaders.CONTENT_LENGTH, getContentLength(file))
                             .body(file);
    }

    public String getContentLength(Resource resource) {
        try {
            return String.valueOf(resource.contentLength());
        } catch (IOException e) {
            throw new FileOperationException(FileOperation.READ, "Content length could not be retrieved");
        }
    }

}
