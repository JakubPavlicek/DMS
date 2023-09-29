package com.dms.service;

import com.dms.entity.DocumentFile;
import com.dms.entity.DocumentFileRevision;
import com.dms.model.DocumentFileRequest;

import java.util.List;

public interface DocumentFileService {
    DocumentFile saveDocumentFile(DocumentFileRequest fileRequest);

    DocumentFile getDocumentFile(String fileId);

    String updateDocumentFile(String fileId, DocumentFile file);

    DocumentFileRevision switchToRevision(String fileId, Long revisionId);

    List<DocumentFileRevision> getRevisions(String fileId);

    String deleteRevision(String fileId, Long revisionId);
}
