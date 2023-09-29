package com.dms.service;

import com.dms.entity.DocumentFile;
import com.dms.entity.DocumentFileRevision;
import com.dms.model.DocumentFileRequest;

import java.util.List;

public interface DocumentFileService {
    DocumentFile saveDocumentFile(DocumentFileRequest fileRequest);

    DocumentFile getDocumentFile(String id);

    String updateDocumentFile(String id, DocumentFile file);

    DocumentFileRevision switchToRevision(String fileId, Long revision);

    List<DocumentFileRevision> getRevisions(String id);

    String deleteRevision(String id, Long revision);
}
