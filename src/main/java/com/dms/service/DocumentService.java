package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentOperation;
import com.dms.entity.DocumentRevision;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.request.DocumentPathRequest;
import com.dms.request.DocumentRequest;
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
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository documentRevisionRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, DocumentRevisionRepository documentRevisionRepository) {
        this.documentRepository = documentRepository;
        this.documentRevisionRepository = documentRevisionRepository;
    }

    public Document getDocument(String documentId) {
        return documentRepository.findById(documentId)
                                 .orElseThrow(() -> new RuntimeException("Soubor s id: " + documentId + " nebyl nalezen."));
    }

    public DocumentRevision getDocumentRevision(String documentId) {
        return documentRevisionRepository.findById(documentId)
                                         .orElseThrow(() -> new RuntimeException("Revize s id: " + documentId + " nebyla nalezena."));
    }

    private DocumentRevision getDocumentRevisionWithId(String documentId, Long revisionId) {
        return documentRevisionRepository.findByDocumentIdAndRevisionId(documentId, revisionId)
                                         .orElseThrow(() -> new RuntimeException("revize nenalezena"));
    }

    public Document saveDocument(DocumentRequest fileRequest) {
        Document document = getDocumentFromRequest(fileRequest);

        return documentRepository.save(document);
    }

    public Document getDocumentFromRequest(DocumentRequest documentRequest) {
        MultipartFile file = documentRequest.getFile();

        String path = StringUtils.cleanPath(file.getOriginalFilename());
        String name = StringUtils.getFilename(path);
        String extension = StringUtils.getFilenameExtension(path);
        String type = file.getContentType();
        String author = documentRequest.getAuthor();
        byte[] data;

        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Data souboru se nepodarilo ziskat.");
        }

        return Document.builder()
                       .name(name)
                       .extension(extension)
                       .type(type)
                       .path(path)
                       .author(author)
                       .operation(DocumentOperation.INSERT)
                       .data(data)
                       .build();
    }

    public String updateDocument(String documentId, DocumentRequest documentRequest) {
        Document databaseFile = getDocument(documentId);
        saveDocumentRevision(databaseFile, databaseFile.getOperation());

        Document document = getDocumentFromRequest(documentRequest);
        document.setDocumentId(documentId);
        document.setOperation(DocumentOperation.UPDATE);

        documentRepository.save(document);

        return "Document updated successfully";
    }

    private void saveDocumentRevision(Document file, DocumentOperation operation) {
        DocumentRevision documentRevision = DocumentRevision.builder()
                                                            .documentId(file.getDocumentId())
                                                            .name(file.getName())
                                                            .extension(file.getExtension())
                                                            .type(file.getType())
                                                            .path(file.getPath())
                                                            .author(file.getAuthor())
                                                            .operation(operation)
                                                            .data(file.getData())
                                                            .build();
        documentRevisionRepository.save(documentRevision);
    }

    @Transactional
    public DocumentRevision switchToRevision(String documentId, Long revisionId) {
        Document databaseFile = getDocument(documentId);
        DocumentRevision documentRevision = getDocumentRevisionWithId(documentId, revisionId);

        saveDocumentRevision(databaseFile, databaseFile.getOperation());
        updateDocumentToRevision(databaseFile, documentRevision);

        documentRevisionRepository.delete(documentRevision);

        return documentRevision;
    }

    private void updateDocumentToRevision(Document databaseFile, DocumentRevision documentRevision) {
        documentRepository.updateDocumentName(databaseFile, documentRevision.getName());
        documentRepository.updateDocumentExtension(databaseFile, documentRevision.getExtension());
        documentRepository.updateDocumentType(databaseFile, documentRevision.getType());
        documentRepository.updateDocumentPath(databaseFile, documentRevision.getPath());
        documentRepository.updateDocumentAuthor(databaseFile, documentRevision.getAuthor());
        documentRepository.updateDocumentOperation(databaseFile, documentRevision.getOperation());
        documentRepository.updateDocumentUpdatedAt(databaseFile, LocalDateTime.now());
        documentRepository.updateDocumentData(databaseFile, documentRevision.getData());
    }

    @Transactional
    public List<DocumentRevision> getRevisions(String documentId) {
        return documentRevisionRepository.findAllByDocumentId(documentId);
    }

    @Transactional
    public String deleteRevision(String documentId, Long revisionId) {
        documentRevisionRepository.deleteByDocumentIdAndRevisionId(documentId, revisionId);

        return "Revision deleted successfully";
    }

    public String deleteDocument(String documentId) {
        Document file = getDocument(documentId);
        saveDocumentRevision(file, DocumentOperation.DELETE);
        documentRepository.delete(file);

        return "Document deleted successfully";
    }

    @Transactional
    public String moveDocument(String documentId, DocumentPathRequest documentPath) {
        Document file = getDocument(documentId);
        documentRepository.updateDocumentPath(file, documentPath.getPath());

        return "Document moved successfully";
    }

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        Document file = getDocument(documentId);

        // TODO: add file UUID to header

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(file.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                             .body(new ByteArrayResource(file.getData()));
    }
}
