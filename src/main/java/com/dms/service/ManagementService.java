package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.FileWithPathAlreadyExistsException;
import com.dms.exception.RevisionNotFoundException;
import com.dms.exception.UnauthorizedAccessException;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class ManagementService {

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository revisionRepository;

    private final BlobStorageService blobStorageService;
    private final UserService userService;

    public User getAuthenticatedUser() {
        return userService.getAuthenticatedUser();
    }

    private boolean isDocumentCreatedByAuthUser(Document document) {
        String documentAuthorEmail = document.getAuthor().getEmail();
        String authenticatedUserEmail = userService.getAuthenticatedUserEmail();
        return documentAuthorEmail.equals(authenticatedUserEmail);
    }

    private boolean isRevisionCreatedByAuthUser(DocumentRevision revision) {
        String revisionAuthorEmail = revision.getAuthor().getEmail();
        String authenticatedUserEmail = userService.getAuthenticatedUserEmail();
        return revisionAuthorEmail.equals(authenticatedUserEmail);
    }

    public Document getDocument(String documentId) {
        log.debug("Getting document: documentId={}", documentId);
        Document document = documentRepository.findByDocumentId(documentId)
                                              .orElseThrow(() -> new DocumentNotFoundException("File with ID: " + documentId + " not found"));

        if (!isDocumentCreatedByAuthUser(document)) {
            throw new UnauthorizedAccessException("Can't access document of someone else");
        }

        return document;
    }

    public DocumentRevision getRevision(String revisionId) {
        log.debug("Getting revision: revisionId={}", revisionId);
        DocumentRevision revision = revisionRepository.findByRevisionId(revisionId)
                                                      .orElseThrow(() -> new RevisionNotFoundException("Revision with ID: " + revisionId + " not found"));

        if (!isRevisionCreatedByAuthUser(revision)) {
            throw new UnauthorizedAccessException("Can't access revision of someone else");
        }

        return revision;
    }

    public List<Document> getDocumentsByUser(User user) {
        return documentRepository.findAllByAuthor(user);
    }

    public DocumentRevision getRevisionByDocumentAndId(Document document, String revisionId) {
        log.debug("Getting revision by document and ID: documentId={}, revisionId={}", document.getDocumentId(), revisionId);
        return revisionRepository.findByDocumentAndRevisionId(document, revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revision with ID: " + revisionId + " not found for document with ID: " + document.getDocumentId()));
    }

    public void validateUniquePath(String path, Document document) {
        log.debug("Validating unique path: path={}, document={}", path, document);

        String filename = document.getName();
        User author = document.getAuthor();

        // user can't have a duplicate path for a document with the same name
        if (documentRepository.pathWithFileAlreadyExists(path, filename, author)) {
            throw new FileWithPathAlreadyExistsException("Document: " + filename + " with path: " + path + " already exists");
        }
    }

    public Document updateDocumentToRevision(Document document, DocumentRevision revision) {
        log.debug("Updating document to revision: documentId={}, revisionId={}", document.getDocumentId(), revision.getRevisionId());

        document.setName(revision.getName());
        document.setType(revision.getType());
        document.setHash(revision.getHash());
        document.setVersion(revision.getVersion());
        document.setAuthor(revision.getAuthor());

        // flush to immediately initialize the "createdAt" and "updatedAt" fields
        Document updatedDocument = documentRepository.saveAndFlush(document);

        log.info("Successfully updated details of document {} from revision {}", document.getDocumentId(), revision.getRevisionId());

        return updatedDocument;
    }

    public void saveRevisionFromDocument(Document document) {
        log.debug("Saving revision from document: document={}", document);

        DocumentRevision documentRevision = DocumentRevision.builder()
                                                            .document(document)
                                                            .version(getLastRevisionVersion(document) + 1)
                                                            .name(document.getName())
                                                            .type(document.getType())
                                                            .author(document.getAuthor())
                                                            .hash(document.getHash())
                                                            .build();

        DocumentRevision savedRevision = revisionRepository.save(documentRevision);

        log.info("Revision {} saved successfully from document {}", savedRevision.getRevisionId(), document.getDocumentId());
    }

    public Long getLastRevisionVersion(Document document) {
        return revisionRepository.findLastRevisionVersionByDocument(document)
                                 .orElse(0L);
    }

    public String storeBlob(MultipartFile file) {
        return blobStorageService.storeBlob(file);
    }

    public Resource getBlob(String hash) {
        return blobStorageService.getBlob(hash);
    }

    public void deleteBlobIfDuplicateHashNotExists(String hash) {
        if (!isDuplicateHashPresent(hash)) {
            blobStorageService.deleteBlob(hash);
        }
    }

    private boolean isDuplicateHashPresent(String hash) {
        return documentRepository.duplicateHashExists(hash) || revisionRepository.duplicateHashExists(hash);
    }

}
