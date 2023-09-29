package com.dms.service;

import com.dms.entity.DocumentFile;
import com.dms.model.DocumentFileRequest;
import com.dms.repository.DocumentFileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
public class DocumentFileServiceImpl implements DocumentFileService {

    private final DocumentFileRepository documentFileRepository;

    @Autowired
    public DocumentFileServiceImpl(DocumentFileRepository documentFileRepository) {
        this.documentFileRepository = documentFileRepository;
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
    public DocumentFile getDocumentFile(String id) {
        return documentFileRepository.findById(id)
                                     .orElseThrow(() -> new RuntimeException("Soubor s id: " + id + " nebyl nalezen."));
    }

    @Override
    public String updateDocumentFile(String id, DocumentFile file) {
        DocumentFile documentFile = documentFileRepository.findById(id)
                                                          .orElseThrow(() -> new RuntimeException("soubor nenalezen"));

        String name = file.getFileName();
        String type = file.getFileType();
        String path = file.getFilePath();
        String author = file.getAuthor();
        byte[] data = file.getData();

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

    @Override
    @Transactional
    public DocumentFile switchToRevision(String fileId, Long revisionId) {
        Revision<Long, DocumentFile> documentFileRevision = documentFileRepository.findRevision(fileId, revisionId)
                                                                                  .orElseThrow(() -> new RuntimeException("revize nenalezena"));

        DocumentFile revisionFile = documentFileRevision.getEntity();
        DocumentFile databaseFile = documentFileRepository.findById(fileId)
                                                          .orElseThrow(() -> new RuntimeException("soubor nenalezen"));

        Instant revisionInstant = documentFileRevision.getRevisionInstant()
                                                      .orElseThrow(() -> new RuntimeException("datum vytvoreni revize nebyl nalezen"));
        LocalDateTime revisionCreatedAt = LocalDateTime.ofInstant(revisionInstant, ZoneId.systemDefault());

        documentFileRepository.updateFileName(databaseFile, revisionFile.getFileName());
        documentFileRepository.updateFileType(databaseFile, revisionFile.getFileType());
        documentFileRepository.updateFilePath(databaseFile, revisionFile.getFilePath());
        documentFileRepository.updateFileAuthor(databaseFile, revisionFile.getAuthor());
        documentFileRepository.updateFileUpdatedAt(databaseFile, revisionCreatedAt);
        documentFileRepository.updateFileData(databaseFile, revisionFile.getData());

        return revisionFile;
    }

    @Override
    public List<Revision<Long, DocumentFile>> getRevisions(String id) {
        return documentFileRepository.findRevisions(id)
                                     .getContent();
    }

    @Override
    @Transactional
    public String deleteRevision(String id, Long revisionId) {
        Revisions<Long, DocumentFile> revisions = documentFileRepository.findRevisions(id);
        Revision<Long, DocumentFile> revision = revisions.stream()
                                                         .filter(rev -> rev.getRevisionNumber()
                                                                           .orElseThrow(() -> new RuntimeException("revize nenalezena"))
                                                                           .equals(revisionId))
                                                         .findFirst()
                                                         .orElseThrow(() -> new RuntimeException("revize nenalezena"));

        DocumentFile file = documentFileRepository.findById(id).orElseThrow(() -> new RuntimeException("soubor nenalezen"));

        // TODO: zabranit smazani hlavni revize ADD (0) - (vytvoreni dokumentu)

        if(revision.getEntity().equals(file))
            throw new RuntimeException("nelze odstranit revizi souboru, ktery je nastaven jako aktualni");

        documentFileRepository.deleteFileHistoryById(revisionId);
        documentFileRepository.deleteRevisionById(revisionId);

        return "Revision deleted successfully";
    }
}
