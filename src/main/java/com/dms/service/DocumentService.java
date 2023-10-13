package com.dms.service;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.UserDTO;
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

    private final DocumentCommonService documentCommonService;
    private final UserService userService;

    public DocumentDTO getDocument(String documentId) {
        Document document = documentCommonService.getDocument(documentId);
        return documentCommonService.mapDocumentToDocumentDto(document);
    }

    public List<DocumentRevisionDTO> getDocumentRevisions(String documentId) {
        Document document = documentCommonService.getDocument(documentId);
        List<DocumentRevision> revisions = document.getRevisions();

        return revisions.stream()
                        .map(documentCommonService::mapRevisionToRevisionDto)
                        .toList();
    }

    private Document getDocumentFromFile(MultipartFile file) {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String path = StringUtils.cleanPath(originalFileName);
        String name = StringUtils.getFilename(path);
        String type = file.getContentType();

        return Document.builder()
                       .name(name)
                       .type(type)
                       .build();
    }

    @Transactional
    public DocumentDTO saveDocument(UserDTO userDto, MultipartFile file) {
        String hash = documentCommonService.storeBlob(file);

        User user = documentCommonService.mapUserDtoToUser(userDto);
        User author = userService.getUser(user);

        Document document = getDocumentFromFile(file);
        document.setHash(hash);
        document.setAuthor(author);

        // flush to immediately initialize the "createdAt" and "updatedAt" fields, ensuring the DTO does not contain null values for these properties
        Document savedDocument = documentRepository.saveAndFlush(document);

        documentCommonService.createDocumentRevision(savedDocument);

        return documentCommonService.mapDocumentToDocumentDto(savedDocument);
    }

    @Transactional
    public String updateDocument(String documentId, UserDTO userDto, MultipartFile file) {
        Document databaseDocument = documentCommonService.getDocument(documentId);

        String hash = documentCommonService.storeBlob(file);

        User user = documentCommonService.mapUserDtoToUser(userDto);
        User author = userService.getUser(user);

        Document document = getDocumentFromFile(file);
        document.setDocumentId(documentId);
        document.setCreatedAt(databaseDocument.getCreatedAt());
        document.setHash(hash);
        document.setAuthor(author);

        documentCommonService.createDocumentRevision(document);

        documentRepository.save(document);

        return "Document updated successfully";
    }

    @Transactional
    public DocumentRevisionDTO switchToVersion(String documentId, Long version) {
        Document document = documentCommonService.getDocument(documentId);
        List<DocumentRevision> revisions = document.getRevisions();

        DocumentRevision revision = revisions.stream()
                                             .filter(rev -> rev.getVersion()
                                                               .equals(version))
                                             .findFirst()
                                             .orElseThrow(() -> new RevisionNotFoundException("Nebyla nalezena revize s verzi: " + version));

        documentCommonService.updateDocumentToRevision(document, revision);

        return documentCommonService.mapRevisionToRevisionDto(revision);
    }

    @Transactional
    public String deleteDocumentWithRevisions(String documentId) {
        Document document = documentCommonService.getDocument(documentId);

        List<DocumentRevision> documentRevisions = document.getRevisions();
        documentRevisions.forEach(revision -> documentCommonService.deleteBlobIfDuplicateHashNotExists(revision.getHash()));

        documentCommonService.deleteBlobIfDuplicateHashNotExists(document.getHash());
        documentRepository.delete(document);

        return "Document deleted successfully";
    }

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        Document document = documentCommonService.getDocument(documentId);
        String hash = document.getHash();
        byte[] data = documentCommonService.getBlob(hash);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(document.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                             .body(new ByteArrayResource(data));
    }

}
