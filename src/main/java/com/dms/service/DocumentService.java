package com.dms.service;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.SortFieldItem;
import com.dms.dto.UserDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private User getUserFromUserDto(UserDTO userDto) {
        User user = documentCommonService.mapUserDtoToUser(userDto);
        return userService.getUser(user);
    }

    private Document createDocumentFromUserDtoAndFile(UserDTO userDto, MultipartFile file) {
        String hash = documentCommonService.storeBlob(file);
        User author = getUserFromUserDto(userDto);

        return createDocument(file, hash, author);
    }

    private Document createDocument(MultipartFile file, String hash, User author) {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String path = StringUtils.cleanPath(originalFileName);
        String name = StringUtils.getFilename(path);
        String type = file.getContentType();

        return Document.builder()
                       .name(name)
                       .type(type)
                       .hash(hash)
                       .author(author)
                       .build();
    }

    @Transactional
    public DocumentDTO saveDocument(UserDTO userDto, MultipartFile file) {
        Document document = createDocumentFromUserDtoAndFile(userDto, file);

        // flush to immediately initialize the "createdAt" and "updatedAt" fields, ensuring the DTO does not contain null values for these properties
        Document savedDocument = documentRepository.saveAndFlush(document);

        documentCommonService.saveRevisionFromDocument(savedDocument);

        return documentCommonService.mapDocumentToDocumentDto(savedDocument);
    }

    @Transactional
    public String updateDocument(String documentId, UserDTO userDto, MultipartFile file) {
        Document document = createDocumentFromUserDtoAndFile(userDto, file);
        document.setDocumentId(documentId);

        documentCommonService.saveRevisionFromDocument(document);
        documentRepository.save(document);

        return "Document updated successfully";
    }

    @Transactional
    public DocumentRevisionDTO switchToVersion(String documentId, Long version) {
        Document document = documentCommonService.getDocument(documentId);
        DocumentRevision revision = documentCommonService.getRevisionByDocumentAndVersion(document, version);

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

    public Page<DocumentDTO> getDocumentsWithPagingAndSorting(int pageNumber, int pageSize, List<SortFieldItem> sortFieldItems) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        if (Objects.nonNull(sortFieldItems)) {
            Sort sort = documentCommonService.getSortFromFields(sortFieldItems);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        }

        Page<Document> documents = documentRepository.findAll(pageable);
        long totalDocuments = documentRepository.count();

        List<DocumentDTO> documentDTOs = documents.stream()
                                                  .map(documentCommonService::mapDocumentToDocumentDto)
                                                  .toList();

        return new PageImpl<>(documentDTOs, pageable, totalDocuments);
    }

}
