package com.dms.service;

import com.dms.entity.DocumentFile;
import com.dms.entity.DocumentFileRevision;
import com.dms.model.DocumentFileRequest;
import com.dms.model.FileOperation;
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
import java.util.Objects;
import java.util.Optional;

@Service
public class DocumentFileService {
    private final DocumentFileRepository documentFileRepository;
    private final DocumentFileRevisionRepository documentFileRevisionRepository;

    @Autowired
    public DocumentFileService(DocumentFileRepository documentFileRepository, DocumentFileRevisionRepository documentFileRevisionRepository) {
        this.documentFileRepository = documentFileRepository;
        this.documentFileRevisionRepository = documentFileRevisionRepository;
    }

    public DocumentFile saveDocumentFile(DocumentFileRequest fileRequest) {
        MultipartFile file = fileRequest.getFile();

        String path = StringUtils.cleanPath(file.getOriginalFilename());
        String name = StringUtils.getFilename(path);
        String extension = StringUtils.getFilenameExtension(path);
        String type = file.getContentType();
        String author = fileRequest.getAuthor();
        byte[] fileData;

        try {
            fileData = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DocumentFile documentFile = DocumentFile.builder()
                                                .fileName(name)
                                                .fileExtension(extension)
                                                .fileType(type)
                                                .author(author)
                                                .data(fileData)
                                                .build();

        return documentFileRepository.save(documentFile);
    }

    public DocumentFile getDocumentFile(String fileId) {
        return documentFileRepository.findById(fileId)
                                     .orElseThrow(() -> new RuntimeException("Soubor s id: " + fileId + " nebyl nalezen."));
    }

    public String updateDocumentFile(String fileId, DocumentFile file) {
        DocumentFile documentFile = documentFileRepository.findById(fileId)
                                                          .orElseThrow(() -> new RuntimeException("soubor nenalezen"));

        String name = file.getFileName();
        String extension = file.getFileExtension();
        String type = file.getFileType();
        String path = file.getFilePath();
        String author = file.getAuthor();
        byte[] data = file.getData();

        FileOperation operation = documentFileRevisionRepository.existsByFileId(fileId) ? FileOperation.UPDATE : FileOperation.INSERT;
        saveDocumentFileRevision(documentFile, operation);

        // TODO: add Validation and remove if statements

        if (Objects.nonNull(name))
            documentFile.setFileName(name);

        if (Objects.nonNull(extension))
            documentFile.setFileExtension(extension);

        if (Objects.nonNull(type))
            documentFile.setFileType(type);

        if (Objects.nonNull(path))
            documentFile.setFilePath(path);

        if (Objects.nonNull(author))
            documentFile.setAuthor(author);

        if (Objects.nonNull(data))
            documentFile.setData(data);

        documentFileRepository.save(documentFile);

        return "Document file updated successfully";
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
        DocumentFile databaseFile = documentFileRepository.findById(fileId)
                                                          .orElseThrow(() -> new RuntimeException("soubor nenalezen"));

        DocumentFileRevision documentFileRevision = documentFileRevisionRepository.findByFileIdAndRevisionId(fileId, revisionId)
                                                                                  .orElseThrow(() -> new RuntimeException("revize nenalezena"));

        updateDocumentFileToRevision(databaseFile, documentFileRevision);

        return documentFileRevision;
    }

    private void updateDocumentFileToRevision(DocumentFile databaseFile, DocumentFileRevision documentFileRevision) {
        documentFileRepository.updateFileName(databaseFile, documentFileRevision.getFileName());
        documentFileRepository.updateFileExtension(databaseFile, documentFileRevision.getFileExtension());
        documentFileRepository.updateFileType(databaseFile, documentFileRevision.getFileType());
        documentFileRepository.updateFilePath(databaseFile, documentFileRevision.getFilePath());
        documentFileRepository.updateFileAuthor(databaseFile, documentFileRevision.getAuthor());
        documentFileRepository.updateFileUpdatedAt(databaseFile, LocalDateTime.now());
        documentFileRepository.updateFileData(databaseFile, documentFileRevision.getData());
    }

    @Transactional
    public List<DocumentFileRevision> getRevisions(String fileId) {
        return documentFileRevisionRepository.findAllByFileId(fileId);
    }

    @Transactional
    public String deleteRevision(String fileId, Long revisionId) {
        Optional<DocumentFileRevision> documentFileRevision = documentFileRevisionRepository.findByFileIdAndRevisionId(fileId, revisionId);

        if (documentFileRevision.isEmpty())
            throw new RuntimeException("Revize nenalezena");

        documentFileRevisionRepository.deleteByFileIdAndRevisionId(fileId, revisionId);
        return "Revision deleted successfully";
    }

    public String deleteDocumentFile(String fileId) {
        DocumentFile file = documentFileRepository.findById(fileId)
                                                  .orElseThrow(() -> new RuntimeException("soubor nenalezen"));

        saveDocumentFileRevision(file, FileOperation.DELETE);

        documentFileRepository.delete(file);
        return "File deleted successfully";
    }

    @Transactional
    public String moveDocumentFile(String fileId, String filePath) {
        DocumentFile file = documentFileRepository.findById(fileId)
                                                  .orElseThrow(() -> new RuntimeException("soubor nenalezen"));

        documentFileRepository.updateFilePath(file, filePath);
        return "File moved successfully";
    }

    public ResponseEntity<Resource> downloadDocumentFile(String fileId) {
        DocumentFile file = documentFileRepository.findById(fileId)
                                                  .orElseThrow(() -> new RuntimeException("soubor nenalezen"));

        // TODO: add file UUID to header

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(file.getFileType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                             .body(new ByteArrayResource(file.getData()));
    }
}
