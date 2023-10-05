package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentOperation;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.repository.UserRepository;
import com.dms.request.DocumentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Objects;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository documentRevisionRepository;
    private final UserRepository userRepository;

    private final BlobStorageService blobStorageService;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, DocumentRevisionRepository documentRevisionRepository, UserRepository userRepository, BlobStorageService blobStorageService) {
        this.documentRepository = documentRepository;
        this.documentRevisionRepository = documentRevisionRepository;
        this.userRepository = userRepository;
        this.blobStorageService = blobStorageService;
    }

    public Document getDocument(String documentId) {
        return documentRepository.findById(documentId)
                                 .orElseThrow(() -> new RuntimeException("Soubor s id: " + documentId + " nebyl nalezen."));
    }

    public DocumentRevision getDocumentRevision(String documentId, Long revisionId) {
        return documentRevisionRepository.findByDocument_DocumentIdAndRevisionId(documentId, revisionId)
                                         .orElseThrow(() -> new RuntimeException("revize nenalezena"));
    }

    private User getUserFromRequest(DocumentRequest documentRequest) {
        try {
            return new ObjectMapper().readValue(documentRequest.getUser(), User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Nepodarilo se ziskat uzivatele z JSONu");
        }
    }

    private Document getDocumentFromRequest(DocumentRequest documentRequest) {
        MultipartFile file = documentRequest.getFile();

        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String path = StringUtils.cleanPath(originalFileName);
        String name = StringUtils.getFilename(path);
        String extension = StringUtils.getFilenameExtension(path);
        String type = file.getContentType();

        return Document.builder()
                       .name(name)
                       .extension(extension)
                       .type(type)
                       .path(path)
                       .operation(DocumentOperation.INSERT)
                       .build();
    }

    private Long getLastRevisionVersion(Document document) {
        return documentRevisionRepository.findLastRevisionVersionByDocument(document)
                                         .orElse(0L);
    }

    private void createDocumentRevision(Document document) {
        DocumentRevision documentRevision = DocumentRevision.builder()
                                                            .document(document)
                                                            .version(getLastRevisionVersion(document) + 1)
                                                            .name(document.getName())
                                                            .extension(document.getExtension())
                                                            .type(document.getType())
                                                            .path(document.getPath())
                                                            .author(document.getAuthor())
                                                            .operation(document.getOperation())
                                                            .hashPointer(document.getHashPointer())
                                                            .build();
        documentRevisionRepository.save(documentRevision);
    }

    private User saveUser(User user) {
        String username = user.getUsername();
        String email = user.getEmail();

        if (userRepository.existsByUsernameAndEmail(username, email))
            return userRepository.findByUsernameAndEmail(username, email)
                                 .orElseThrow(() -> new RuntimeException("Uzivatel nebyl nalezen"));

        return userRepository.save(user);
    }

    @Transactional
    public Document saveDocument(DocumentRequest documentRequest) {
        MultipartFile file = documentRequest.getFile();
        String hash = blobStorageService.storeBlob(file);

        User userFromRequest = getUserFromRequest(documentRequest);
        User author = saveUser(userFromRequest);

        Document document = getDocumentFromRequest(documentRequest);
        document.setHashPointer(hash);
        document.setAuthor(author);

        Document savedDocument = documentRepository.save(document);

        createDocumentRevision(savedDocument);

        return savedDocument;
    }

    public String updateDocument(String documentId, DocumentRequest documentRequest) {
        Document databaseDocument = getDocument(documentId);

        MultipartFile file = documentRequest.getFile();
        String hash = blobStorageService.storeBlob(file);

        User userFromRequest = getUserFromRequest(documentRequest);
        User author = saveUser(userFromRequest);

        Document document = getDocumentFromRequest(documentRequest);
        document.setDocumentId(documentId);
        document.setCreatedAt(databaseDocument.getCreatedAt());
        document.setOperation(DocumentOperation.UPDATE);
        document.setHashPointer(hash);
        document.setAuthor(author);

        createDocumentRevision(document);

        documentRepository.save(document);

        return "Document updated successfully";
    }

    @Transactional
    public DocumentRevision switchToRevision(String documentId, Long revisionId) {
        Document databaseDocument = getDocument(documentId);
        DocumentRevision documentRevision = getDocumentRevision(documentId, revisionId);

        updateDocumentToRevision(databaseDocument, documentRevision);

        return documentRevision;
    }

    private void updateDocumentToRevision(Document document, DocumentRevision documentRevision) {
        document.setName(documentRevision.getName());
        document.setExtension(documentRevision.getExtension());
        document.setType(documentRevision.getType());
        document.setPath(documentRevision.getPath());
        document.setAuthor(documentRevision.getAuthor());
        document.setOperation(documentRevision.getOperation());
        document.setHashPointer(documentRevision.getHashPointer());

        documentRepository.save(document);
    }

    private void updateRevisionVersionsForDocument(String documentId) {
        Document document = getDocument(documentId);
        List<DocumentRevision> documentRevisions = documentRevisionRepository.findAllByDocumentOrderByCreatedAtAsc(document);

        Long version = 1L;
        for (DocumentRevision revision : documentRevisions) {
            documentRevisionRepository.updateVersion(revision, version);
            version++;
        }
    }

    @Transactional
    public List<DocumentRevision> getRevisions(String documentId) {
        Document document = getDocument(documentId);
        return document.getRevisions();
    }

    @Transactional
    public String deleteRevision(String documentId, Long revisionId) {
        DocumentRevision documentRevision = getDocumentRevision(documentId, revisionId);
        String hash = documentRevision.getHashPointer();

        if (isRevisionSetAsCurrent(documentId, revisionId))
            replaceDocumentWithAdjacentRevision(documentId, revisionId);

        blobStorageService.deleteBlob(hash);
        documentRevisionRepository.delete(documentRevision);

        updateRevisionVersionsForDocument(documentId);

        return "Revision deleted successfully";
    }

    private void replaceDocumentWithAdjacentRevision(String documentId, Long currentRevisionId) {
        Document document = getDocument(documentId);
        DocumentRevision currentDocumentRevision = getDocumentRevision(documentId, currentRevisionId);

        Long currentVersion = currentDocumentRevision.getVersion();
        DocumentRevision newRevision = documentRevisionRepository.findPreviousByDocumentAndVersion(document, currentVersion)
                                                                      .orElse(documentRevisionRepository.findNextByDocumentAndVersion(document, currentVersion)
                                                                                                        .orElse(null));
        if(newRevision == null)
            throw new RuntimeException("nebyla nalezena nahrazujici revize pro revizi " + currentRevisionId);

        updateDocumentToRevision(document, newRevision);
    }

    private boolean isRevisionSetAsCurrent(String documentId, Long revisionId) {
        Document document = getDocument(documentId);
        DocumentRevision documentRevision = getDocumentRevision(documentId, revisionId);

        return document.getHashPointer()
                       .equals(documentRevision.getHashPointer());
    }

    @Transactional
    public String deleteDocumentWithRevisions(String documentId) {
        List<DocumentRevision> documentRevisions = getRevisions(documentId);
        documentRevisions.forEach(revision -> blobStorageService.deleteBlob(revision.getHashPointer()));

        Document document = getDocument(documentId);
        blobStorageService.deleteBlob(document.getHashPointer());
        documentRepository.delete(document);

        return "Document deleted successfully";
    }

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        Document document = getDocument(documentId);
        String hash = document.getHashPointer();
        byte[] data = blobStorageService.getBlob(hash);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(document.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                             .body(new ByteArrayResource(data));
    }

    public ResponseEntity<Resource> downloadDocumentRevision(String documentId, Long revisionId) {
        DocumentRevision documentRevision = getDocumentRevision(documentId, revisionId);
        String hash = documentRevision.getHashPointer();
        byte[] data = blobStorageService.getBlob(hash);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(documentRevision.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documentRevision.getName() + "\"")
                             .body(new ByteArrayResource(data));
    }
}
