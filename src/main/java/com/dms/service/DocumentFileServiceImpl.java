package com.dms.service;

import com.dms.entity.DocumentFile;
import com.dms.repository.DocumentFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class DocumentFileServiceImpl implements DocumentFileService {

    private final DocumentFileRepository documentFileRepository;

    @Autowired
    public DocumentFileServiceImpl(DocumentFileRepository documentFileRepository) {
        this.documentFileRepository = documentFileRepository;
    }

    @Override
    public DocumentFile saveFile(MultipartFile file) {
        String filePath = file.getOriginalFilename();
        String fileName = StringUtils.getFilename(filePath);
        String fileType = StringUtils.getFilenameExtension(filePath);
        byte[] fileData;

        try {
            fileData = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DocumentFile documentFile = DocumentFile.builder()
                                                .fileName(fileName)
                                                .fileType(fileType)
                                                .data(fileData)
                                                .build();

        return documentFileRepository.save(documentFile);
    }

    @Override
    public DocumentFile getDocumentFile(String id) {
        return documentFileRepository.findById(id).orElseThrow(() -> new RuntimeException("Soubor s id: " + id + " nebyl nalezen."));
    }

    @Override
    public String updateDocumentFile(String id, DocumentFile file) {
        Optional<DocumentFile> documentFileById = documentFileRepository.findById(id);

        DocumentFile documentFile = documentFileById.get();

        documentFile.setFilePath(file.getFilePath());
        documentFileRepository.save(documentFile);

//        documentFileRepository.updateDocumentFileLocationById(id, location);
        return "Document file updated successfully";
    }
}
