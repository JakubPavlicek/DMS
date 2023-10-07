package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.repository.DocumentRepository;
import com.dms.request.DocumentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    private final DocumentRevisionService revisionService;
    private final UserService userService;
    private final BlobStorageService blobStorageService;

    public Document getDocument(String documentId) {
        return documentRepository.findById(documentId)
                                 .orElseThrow(() -> new RuntimeException("Soubor s id: " + documentId + " nebyl nalezen."));
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
                       .build();
    }

    @Transactional
    public Document saveDocument(DocumentRequest documentRequest) {
        MultipartFile file = documentRequest.getFile();
        String hash = blobStorageService.storeBlob(file);

        User userFromRequest = getUserFromRequest(documentRequest);
        User author = userService.getUser(userFromRequest);

        Document document = getDocumentFromRequest(documentRequest);
        document.setHashPointer(hash);
        document.setAuthor(author);

        Document savedDocument = documentRepository.save(document);

        revisionService.createDocumentRevision(savedDocument);

        return savedDocument;
    }

    public String updateDocument(String documentId, DocumentRequest documentRequest) {
        Document databaseDocument = getDocument(documentId);

        MultipartFile file = documentRequest.getFile();
        String hash = blobStorageService.storeBlob(file);

        User userFromRequest = getUserFromRequest(documentRequest);
        User author = userService.getUser(userFromRequest);

        Document document = getDocumentFromRequest(documentRequest);
        document.setDocumentId(documentId);
        document.setCreatedAt(databaseDocument.getCreatedAt());
        document.setHashPointer(hash);
        document.setAuthor(author);

        revisionService.createDocumentRevision(document);

        documentRepository.save(document);

        return "Document updated successfully";
    }

    @Transactional
    public DocumentRevision switchToVersion(String documentId, Long version) {
        Document document = getDocument(documentId);
        List<DocumentRevision> revisions = document.getRevisions();

        DocumentRevision revision = revisions.stream()
                                             .filter(rev -> rev.getVersion()
                                                               .equals(version))
                                             .findFirst()
                                             .orElseThrow(() -> new RuntimeException("nebyla nalezena revize s verzi: " + version));

        updateDocumentToRevision(document, revision);

        return revision;
    }

    public void updateDocumentToRevision(Document document, DocumentRevision documentRevision) {
        document.setName(documentRevision.getName());
        document.setExtension(documentRevision.getExtension());
        document.setType(documentRevision.getType());
        document.setPath(documentRevision.getPath());
        document.setAuthor(documentRevision.getAuthor());
        document.setHashPointer(documentRevision.getHashPointer());

        documentRepository.save(document);
    }

    public List<DocumentRevision> getRevisions(String documentId) {
        Document document = getDocument(documentId);
        return document.getRevisions();
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
}
