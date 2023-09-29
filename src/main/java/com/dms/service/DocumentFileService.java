package com.dms.service;

import com.dms.entity.DocumentFile;
import com.dms.model.DocumentFileRequest;
import org.springframework.data.history.Revision;

import java.util.List;

public interface DocumentFileService {
    DocumentFile saveDocumentFile(DocumentFileRequest fileRequest);

    DocumentFile getDocumentFile(String id);

    String updateDocumentFile(String id, DocumentFile file);

    DocumentFile switchToRevision(String fileId, Long revision);

    List<Revision<Long, DocumentFile>> getRevisions(String id);

    String deleteRevision(String id, Long revision);
}
