package com.dms.service;

import com.dms.entity.DocumentFile;
import com.dms.model.DocumentFileRequest;

public interface DocumentFileService {
    DocumentFile saveDocumentFile(DocumentFileRequest fileRequest);

    DocumentFile getDocumentFile(String id);

    String updateDocumentFile(String id, DocumentFile file);

    DocumentFile setDocumentFileAsCurrent(String fileId, Long revision);
}
