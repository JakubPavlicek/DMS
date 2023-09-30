package com.dms.service;

import com.dms.entity.DocumentFile;
import com.dms.entity.DocumentFileRevision;
import com.dms.request.DocumentFileRequest;
import com.dms.entity.FileOperation;
import com.dms.repository.DocumentFileRepository;
import com.dms.repository.DocumentFileRevisionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentFileService {
    private final DocumentFileRepository documentFileRepository;
    private final DocumentFileRevisionRepository documentFileRevisionRepository;

    @Autowired
    public DocumentFileService(DocumentFileRepository documentFileRepository, DocumentFileRevisionRepository documentFileRevisionRepository) {
        this.documentFileRepository = documentFileRepository;
        this.documentFileRevisionRepository = documentFileRevisionRepository;
    }

    public DocumentFile getDocumentFile(String fileId) {
        return documentFileRepository.findById(fileId)
                                     .orElseThrow(() -> new RuntimeException("Soubor s id: " + fileId + " nebyl nalezen."));
    }

    public DocumentFileRevision getDocumentFileRevision(String fileId) {
        return documentFileRevisionRepository.findById(fileId)
                                             .orElseThrow(() -> new RuntimeException("Revize s id: " + fileId + " nebyla nalezena."));
    }

    private DocumentFileRevision getDocumentFileRevisionWithId(String fileId, Long revisionId) {
        return documentFileRevisionRepository.findByFileIdAndRevisionId(fileId, revisionId)
                                             .orElseThrow(() -> new RuntimeException("revize nenalezena"));
    }

    public DocumentFile saveDocumentFile(DocumentFileRequest fileRequest) {
        DocumentFile documentFile = getDocumentFileFromRequest(fileRequest);

        return documentFileRepository.save(documentFile);
    }

    public DocumentFile getDocumentFileFromRequest(DocumentFileRequest fileRequest) {
        MultipartFile file = fileRequest.getFile();

        String path = StringUtils.cleanPath(file.getOriginalFilename());
        String name = StringUtils.getFilename(path);
        String extension = StringUtils.getFilenameExtension(path);
        String type = file.getContentType();
        String author = fileRequest.getAuthor();
        byte[] data;

        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Data souboru se nepodarilo ziskat.");
        }

        return DocumentFile.builder()
                           .fileName(name)
                           .fileExtension(extension)
                           .fileType(type)
                           .filePath(path)
                           .author(author)
                           .fileOperation(FileOperation.INSERT)
                           .data(data)
                           .build();
    }

    public String updateDocumentFile(String fileId, DocumentFileRequest fileRequest) {
        DocumentFile databaseFile = getDocumentFile(fileId);
        saveDocumentFileRevision(databaseFile, databaseFile.getFileOperation());

        DocumentFile documentFile = getDocumentFileFromRequest(fileRequest);
        documentFile.setFileId(fileId);
        documentFile.setFileOperation(FileOperation.UPDATE);

        documentFileRepository.save(documentFile);

        return "File updated successfully";
    }

    private void saveDocumentFileRevision(DocumentFile file, FileOperation operation) {
        DocumentFileRevision documentFileRevision = DocumentFileRevision.builder()
                                                                        .fileId(file.getFileId())
                                                                        .fileName(file.getFileName())
                                                                        .fileExtension(file.getFileExtension())
                                                                        .fileType(file.getFileType())
                                                                        .filePath(file.getFilePath())
                                                                        .author(file.getAuthor())
                                                                        .fileOperation(operation)
                                                                        .data(file.getData())
                                                                        .build();
        documentFileRevisionRepository.save(documentFileRevision);
    }

    @Transactional
    public DocumentFileRevision switchToRevision(String fileId, Long revisionId) {
        DocumentFile databaseFile = getDocumentFile(fileId);
        DocumentFileRevision documentFileRevision = getDocumentFileRevisionWithId(fileId, revisionId);

        saveDocumentFileRevision(databaseFile, databaseFile.getFileOperation());
        updateDocumentFileToRevision(databaseFile, documentFileRevision);

        documentFileRevisionRepository.delete(documentFileRevision);

        return documentFileRevision;
    }

    private void updateDocumentFileToRevision(DocumentFile databaseFile, DocumentFileRevision documentFileRevision) {
        documentFileRepository.updateFileName(databaseFile, documentFileRevision.getFileName());
        documentFileRepository.updateFileExtension(databaseFile, documentFileRevision.getFileExtension());
        documentFileRepository.updateFileType(databaseFile, documentFileRevision.getFileType());
        documentFileRepository.updateFilePath(databaseFile, documentFileRevision.getFilePath());
        documentFileRepository.updateFileAuthor(databaseFile, documentFileRevision.getAuthor());
        documentFileRepository.updateFileOperation(databaseFile, documentFileRevision.getFileOperation());
        documentFileRepository.updateFileUpdatedAt(databaseFile, LocalDateTime.now());
        documentFileRepository.updateFileData(databaseFile, documentFileRevision.getData());
    }

    @Transactional
    public List<DocumentFileRevision> getRevisions(String fileId) {
        return documentFileRevisionRepository.findAllByFileId(fileId);
    }

    @Transactional
    public String deleteRevision(String fileId, Long revisionId) {
        DocumentFileRevision documentFileRevision = getDocumentFileRevisionWithId(fileId, revisionId);
        documentFileRevisionRepository.deleteByFileIdAndRevisionId(fileId, revisionId);

        return "Revision deleted successfully";
    }

    public String deleteDocumentFile(String fileId) {
        DocumentFile file = getDocumentFile(fileId);
        saveDocumentFileRevision(file, FileOperation.DELETE);
        documentFileRepository.delete(file);

        return "File deleted successfully";
    }

    @Transactional
    public String moveDocumentFile(String fileId, String filePath) {
        DocumentFile file = getDocumentFile(fileId);
        documentFileRepository.updateFilePath(file, filePath);

        return "File moved successfully";
    }

    public ResponseEntity<Resource> downloadDocumentFile(String fileId) {
        DocumentFile file = getDocumentFile(fileId);

        // TODO: add file UUID to header

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(file.getFileType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                             .body(new ByteArrayResource(file.getData()));
    }
}
