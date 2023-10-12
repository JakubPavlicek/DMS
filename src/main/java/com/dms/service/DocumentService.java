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
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    public DocumentDTO getDocument(String documentId) {
        Document document = documentServiceCommon.getDocument(documentId);
        return modelMapper.map(document, DocumentDTO.class);
    }

    public List<DocumentRevisionDTO> getDocumentRevisions(String documentId) {
        Document document = documentServiceCommon.getDocument(documentId);
        List<DocumentRevision> revisions = document.getRevisions();

        return revisions.stream()
                        .map(revision -> modelMapper.map(revision, DocumentRevisionDTO.class))
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
        String hash = blobStorageService.storeBlob(file);

        User user = modelMapper.map(userDto, User.class);
        User author = userService.getUser(user);

        Document document = getDocumentFromFile(file);
        document.setHash(hash);
        document.setAuthor(author);

        Document savedDocument = documentRepository.saveAndFlush(document);

        documentServiceCommon.createDocumentRevision(savedDocument);

        return modelMapper.map(savedDocument, DocumentDTO.class);
    }

    @Transactional
    public String updateDocument(String documentId, UserDTO userDto, MultipartFile file) {
        Document databaseDocument = documentServiceCommon.getDocument(documentId);

        String hash = blobStorageService.storeBlob(file);

        User user = modelMapper.map(userDto, User.class);
        User author = userService.getUser(user);

        Document document = getDocumentFromFile(file);
        document.setDocumentId(documentId);
        document.setCreatedAt(databaseDocument.getCreatedAt());
        document.setHash(hash);
        document.setAuthor(author);

        documentServiceCommon.createDocumentRevision(document);

        documentRepository.save(document);

        return "Document updated successfully";
    }

    @Transactional
    public DocumentRevisionDTO switchToVersion(String documentId, Long version) {
        Document document = documentServiceCommon.getDocument(documentId);
        List<DocumentRevision> revisions = document.getRevisions();

        DocumentRevision revision = revisions.stream()
                                             .filter(rev -> rev.getVersion()
                                                               .equals(version))
                                             .findFirst()
                                             .orElseThrow(() -> new RevisionNotFoundException("Nebyla nalezena revize s verzi: " + version));

        documentServiceCommon.updateDocumentToRevision(document, revision);

        return modelMapper.map(revision, DocumentRevisionDTO.class);
    }

    @Transactional
    public String deleteDocumentWithRevisions(String documentId) {
        Document document = documentServiceCommon.getDocument(documentId);

        List<DocumentRevision> documentRevisions = document.getRevisions();
        documentRevisions.forEach(revision -> documentServiceCommon.deleteBlobIfDuplicateHashNotExists(revision.getHash()));

        documentServiceCommon.deleteBlobIfDuplicateHashNotExists(document.getHash());
        documentRepository.delete(document);

        return "Document deleted successfully";
    }

    public ResponseEntity<Resource> downloadDocument(String documentId) {
        Document document = documentServiceCommon.getDocument(documentId);
        String hash = document.getHash();
        byte[] data = blobStorageService.getBlob(hash);

        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(document.getType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                             .body(new ByteArrayResource(data));
    }

}
