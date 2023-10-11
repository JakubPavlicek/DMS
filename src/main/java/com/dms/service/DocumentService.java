package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.RevisionNotFoundException;
import com.dms.repository.DocumentRepository;
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

    private final DocumentServiceCommon documentServiceCommon;
    private final UserService userService;
    private final BlobStorageService blobStorageService;

    public Document getDocument(String documentId) {
        return documentServiceCommon.getDocument(documentId);
    }

    public List<DocumentRevision> getDocumentRevisions(String documentId) {
        Document document = getDocument(documentId);
        return document.getRevisions();
    }

    private Document getDocumentFromFile(MultipartFile file) {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String path = StringUtils.cleanPath(originalFileName);
        String name = StringUtils.getFilename(path);
        String extension = StringUtils.getFilenameExtension(path);
        String type = file.getContentType();

        return Document.builder()
                       .name(name)
                       .extension(extension)
                       .type(type)
                       .build();
    }

    @Transactional
    public Document saveDocument(User user, MultipartFile file) {
        String hash = blobStorageService.storeBlob(file);
        User author = userService.getUser(user);

        Document document = getDocumentFromFile(file);
        document.setHashPointer(hash);
        document.setAuthor(author);

        Document savedDocument = documentRepository.save(document);

        documentServiceCommon.createDocumentRevision(savedDocument);

        return savedDocument;
    }

    @Transactional
    public String updateDocument(String documentId, User user, MultipartFile file) {
        Document databaseDocument = getDocument(documentId);

        String hash = blobStorageService.storeBlob(file);
        User author = userService.getUser(user);

        Document document = getDocumentFromFile(file);
        document.setDocumentId(documentId);
        document.setCreatedAt(databaseDocument.getCreatedAt());
        document.setHashPointer(hash);
        document.setAuthor(author);

        documentServiceCommon.createDocumentRevision(document);

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
                                             .orElseThrow(() -> new RevisionNotFoundException("nebyla nalezena revize s verzi: " + version));

        documentServiceCommon.updateDocumentToRevision(document, revision);

        return revision;
    }

    @Transactional
    public String deleteDocumentWithRevisions(String documentId) {
        Document document = documentServiceCommon.getDocument(documentId);

        List<DocumentRevision> documentRevisions = document.getRevisions();
        documentRevisions.forEach(revision -> documentServiceCommon.deleteBlobIfDuplicateHashNotExists(revision.getHashPointer()));

        documentRepository.delete(document);
        documentServiceCommon.deleteBlobIfDuplicateHashNotExists(document.getHashPointer());

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
