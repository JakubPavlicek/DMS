package com.dms.service;

import com.dms.entity.DocumentFile;
import com.dms.entity.DocumentFileRevision;
import com.dms.model.DocumentFileRequest;
import com.dms.model.FileOperation;
import com.dms.repository.DocumentFileRepository;
import com.dms.repository.DocumentFileRevisionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DocumentFileServiceImpl implements DocumentFileService {

    private final DocumentFileRepository documentFileRepository;
    private final DocumentFileRevisionRepository documentFileRevisionRepository;

    @Autowired
    public DocumentFileServiceImpl(DocumentFileRepository documentFileRepository, DocumentFileRevisionRepository documentFileRevisionRepository) {
        this.documentFileRepository = documentFileRepository;
        this.documentFileRevisionRepository = documentFileRevisionRepository;
    }

    @Override
    public DocumentFile saveDocumentFile(DocumentFileRequest fileRequest) {
        MultipartFile file = fileRequest.getFile();

        String path = file.getOriginalFilename();
        String name = StringUtils.getFilename(path);
        String type = StringUtils.getFilenameExtension(path);
        String author = fileRequest.getAuthor();
        byte[] fileData;

        try {
            fileData = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DocumentFile documentFile = DocumentFile.builder()
                                                .fileName(name)
                                                .fileType(type)
                                                .author(author)
                                                .data(fileData)
                                                .build();

        return documentFileRepository.save(documentFile);
    }

    @Override
    public DocumentFile getDocumentFile(String fileId) {
        return documentFileRepository.findById(fileId)
                                     .orElseThrow(() -> new RuntimeException("Soubor s id: " + fileId + " nebyl nalezen."));
    }

    @Override
    public String updateDocumentFile(String fileId, DocumentFile file) {
        DocumentFile documentFile = documentFileRepository.findById(fileId)
                                                          .orElseThrow(() -> new RuntimeException("soubor nenalezen"));

        String name = file.getFileName();
        String type = file.getFileType();
        String path = file.getFilePath();
        String author = file.getAuthor();
        byte[] data = file.getData();

        saveDocumentFileRevision(fileId, documentFile);

        if (Objects.nonNull(name))
            documentFile.setFileName(name);

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

    private void saveDocumentFileRevision(String fileId, DocumentFile file) {
        FileOperation operation = documentFileRevisionRepository.existsByFileId(fileId) ? FileOperation.UPDATE : FileOperation.INSERT;

        DocumentFileRevision documentFileRevision = DocumentFileRevision.builder()
                                                                        .fileId(fileId)
                                                                        .fileName(file.getFileName())
                                                                        .fileType(file.getFileType())
                                                                        .filePath(file.getFilePath())
                                                                        .author(file.getAuthor())
                                                                        .fileOperation(operation)
                                                                        .data(file.getData())
                                                                        .build();
        documentFileRevisionRepository.save(documentFileRevision);
    }

    @Override
    @Transactional
    public DocumentFileRevision switchToRevision(String fileId, Long revisionId) {
        DocumentFile databaseFile = documentFileRepository.findById(fileId)
                                                          .orElseThrow(() -> new RuntimeException("soubor nenalezen"));

        DocumentFileRevision documentFileRevision = documentFileRevisionRepository.findByFileIdAndRevisionId(fileId, revisionId)
                                                                                  .orElseThrow(() -> new RuntimeException("revize nenalezena"));

        documentFileRepository.updateFileName(databaseFile, documentFileRevision.getFileName());
        documentFileRepository.updateFileType(databaseFile, documentFileRevision.getFileType());
        documentFileRepository.updateFilePath(databaseFile, documentFileRevision.getFilePath());
        documentFileRepository.updateFileAuthor(databaseFile, documentFileRevision.getAuthor());
        documentFileRepository.updateFileUpdatedAt(databaseFile, LocalDateTime.now());
        documentFileRepository.updateFileData(databaseFile, documentFileRevision.getData());

        return documentFileRevision;
    }

    @Override
    @Transactional
    public List<DocumentFileRevision> getRevisions(String fileId) {
        return documentFileRevisionRepository.findAllByFileId(fileId);
    }

    @Override
    @Transactional
    public String deleteRevision(String fileId, Long revisionId) {
        Optional<DocumentFileRevision> documentFileRevision = documentFileRevisionRepository.findByFileIdAndRevisionId(fileId, revisionId);

        if (documentFileRevision.isEmpty())
            throw new RuntimeException("Revize nenalezena");

        documentFileRevisionRepository.deleteByFileIdAndRevisionId(fileId, revisionId);
        return "Revision deleted successfully";
    }
}
