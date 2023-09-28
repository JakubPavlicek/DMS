package com.dms.service;

import com.dms.entity.DocumentFile;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentFileService {
    DocumentFile saveFile(MultipartFile file);

    DocumentFile getDocumentFile(String id);

    String updateDocumentFile(String id, DocumentFile file);
}
