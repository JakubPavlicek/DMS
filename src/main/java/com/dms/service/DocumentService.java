package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentOperation;
import com.dms.entity.DocumentRevision;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.request.DocumentDestinationRequest;
import com.dms.request.DocumentRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository documentRevisionRepository;

    private final BlobStorageService blobStorageService;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, DocumentRevisionRepository documentRevisionRepository, BlobStorageService blobStorageService) {
        this.documentRepository = documentRepository;
        this.documentRevisionRepository = documentRevisionRepository;
        this.blobStorageService = blobStorageService;
    }

    public Document getDocument(String documentId) {
        Document document = documentRepository.findById(documentId)
                                              .orElseThrow(() -> new RuntimeException("Soubor s id: " + documentId + " nebyl nalezen."));

        byte[] data = blobStorageService.getBlob(document.getHashPointer());
        document.setData(data);

        return document;
    }

    public DocumentRevision getDocumentRevision(String documentId) {
        DocumentRevision documentRevision = documentRevisionRepository.findById(documentId)
                                                                      .orElseThrow(() -> new RuntimeException("Revize s id: " + documentId + " nebyla nalezena."));

        byte[] data = blobStorageService.getBlob(documentRevision.getHashPointer());
        documentRevision.setData(data);

        return documentRevision;
    }

    public DocumentRevision getDocumentRevisionWithId(String documentId, Long revisionId) {
        DocumentRevision documentRevision = documentRevisionRepository.findByDocumentIdAndRevisionId(documentId, revisionId)
                                                                      .orElseThrow(() -> new RuntimeException("revize nenalezena"));

        byte[] data = blobStorageService.getBlob(documentRevision.getHashPointer());
        documentRevision.setData(data);

        return documentRevision;
    }

    private Document getDocumentFromRequest(DocumentRequest documentRequest) {
        MultipartFile file = documentRequest.getFile();

        String path = StringUtils.cleanPath(file.getOriginalFilename());
        String name = StringUtils.getFilename(path);
        String extension = StringUtils.getFilenameExtension(path);
        String type = file.getContentType();
        String author = documentRequest.getAuthor();

        return Document.builder()
                       .name(name)
                       .extension(extension)
                       .type(type)
                       .path(path)
                       .author(author)
                       .operation(DocumentOperation.INSERT)
                       .build();
    }

    public Document saveDocument(DocumentRequest documentRequest) {
        MultipartFile file = documentRequest.getFile();
        String hash = blobStorageService.storeBlob(file);

        Document document = getDocumentFromRequest(documentRequest);
        document.setHashPointer(hash);

        return documentRepository.save(document);
    }

    public String updateDocument(String documentId, DocumentRequest documentRequest) {
        Document databaseDocument = getDocument(documentId);
        saveDocumentRevision(databaseDocument, databaseDocument.getOperation());

        MultipartFile file = documentRequest.getFile();
        String hash = blobStorageService.storeBlob(file);

        Document document = getDocumentFromRequest(documentRequest);
        document.setDocumentId(documentId);
        document.setCreatedAt(databaseDocument.getCreatedAt());
        document.setOperation(DocumentOperation.UPDATE);
        document.setHashPointer(hash);

        documentRepository.save(document);

        return "Document updated successfully";
    }

    private void saveDocumentRevision(Document document, DocumentOperation operation) {
        DocumentRevision documentRevision = DocumentRevision.builder()
                                                            .documentId(document.getDocumentId())
                                                            .name(document.getName())
                                                            .extension(document.getExtension())
                                                            .type(document.getType())
                                                            .path(document.getPath())
                                                            .author(document.getAuthor())
                                                            .operation(operation)
                                                            .hashPointer(document.getHashPointer())
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
//        documentRepository.updateDocumentData(databaseFile, documentRevision.getData());
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
    public String moveDocument(String documentId, DocumentDestinationRequest documentDestination) {
        Document document = getDocument(documentId);
        documentRepository.updateDocumentPath(document, documentDestination.getDestination());

        return "Document moved successfully";
    }

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        Document document = getDocument(documentId);

        // TODO: add file UUID to header

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(document.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                             .body(new ByteArrayResource(document.getData()));
    }

    public ResponseEntity<Resource> downloadDocumentRevision(String documentId, Long revisionId) {
        DocumentRevision documentRevision = getDocumentRevisionWithId(documentId, revisionId);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(documentRevision.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documentRevision.getName() + "\"")
                             .body(new ByteArrayResource(documentRevision.getData()));
    }

    public String copyDocument(String documentId, DocumentDestinationRequest destination) {
        Document databaseDocument = getDocument(documentId);

        Document document = new Document();
        BeanUtils.copyProperties(databaseDocument, document, "documentId", "path");
        document.setPath(destination.getDestination());

        documentRepository.save(document);

        return "Document copied successfully";
    }
}
