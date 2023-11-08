package com.dms.service;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import com.dms.exception.InvalidRegexInputException;
import com.dms.exception.RevisionNotFoundException;
import com.dms.mapper.dto.DocumentRevisionDTOMapper;
import com.dms.mapper.entity.DocumentMapper;
import com.dms.mapper.entity.RevisionMapper;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
@RequiredArgsConstructor
public class DocumentCommonService {

    private static final String FILTER_REGEX = "(name|type|path):\"([^,]*)\"(?:,|$)";

    private static final String DOCUMENT_SORT_REGEX = "(document_id|name|type|path|version|created_at|updated_at):(asc|desc)(?:,|$)";
    private static final String REVISION_SORT_REGEX = "(revision_id|name|type|version|created_at):(asc|desc)(?:,|$)";

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository revisionRepository;

    private final BlobStorageService blobStorageService;
    private final UserService userService;

    public User getAuthenticatedUser() {
        return userService.getAuthenticatedUser();
    }

    public String getAuthenticatedUserEmail() {
        return userService.getAuthenticatedUserEmail();
    }

    public boolean isDocumentCreatedByAuthUser(Document document) {
        String documentAuthorEmail = document.getAuthor().getEmail();
        String authenticatedUserEmail = getAuthenticatedUserEmail();
        return documentAuthorEmail.equals(authenticatedUserEmail);
    }

    public boolean isRevisionCreatedByAuthUser(DocumentRevision revision) {
        String revisionAuthorEmail = revision.getAuthor().getEmail();
        String authenticatedUserEmail = getAuthenticatedUserEmail();
        return revisionAuthorEmail.equals(authenticatedUserEmail);
    }

    public Document getDocument(String documentId) {
        log.debug("Getting document: documentId={}", documentId);
        return documentRepository.findByDocumentId(documentId)
                                 .orElseThrow(() -> new DocumentNotFoundException("File with ID: " + documentId + " not found"));
    }

    public DocumentRevision getRevision(String revisionId) {
        log.debug("Getting revision: revisionId={}", revisionId);
        return revisionRepository.findByRevisionId(revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revision with ID: " + revisionId + " not found"));
    }

    public DocumentRevision getRevisionByDocumentAndId(Document document, String revisionId) {
        log.debug("Getting revision by document and ID: documentId={}, revisionId={}", document.getDocumentId(), revisionId);
        return revisionRepository.findByDocumentAndRevisionId(document, revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revision with ID: " + revisionId + " not found for document with ID: " + document.getDocumentId()));
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

    public void updateRevisionVersionsForDocument(Document document) {
        log.debug("Updating revision versions for document: documentId={}", document.getDocumentId());

        List<DocumentRevision> documentRevisions = revisionRepository.findAllByDocumentOrderByCreatedAtAsc(document);

        Long version = 1L;
        for (DocumentRevision revision : documentRevisions) {
            revisionRepository.updateVersion(revision, version);
            version++;
        }
    }

    public Page<DocumentRevisionDTO> findRevisions(Specification<DocumentRevision> specification, Pageable pageable) {
        Page<DocumentRevision> revisions = revisionRepository.findAll(specification, pageable);
        List<DocumentRevisionDTO> revisionDTOs = revisions.stream()
                                                          .map(DocumentRevisionDTOMapper::map)
                                                          .toList();

        return new PageImpl<>(revisionDTOs, pageable, revisions.getTotalElements());
    }

    public Map<String, String> getDocumentFilters(String filter) {
        return getFilters(filter, DocumentMapper::getMappedDocumentField);
    }

    public Map<String, String> getRevisionFilters(String filter) {
        return getFilters(filter, RevisionMapper::getMappedRevisionField);
    }

    private Map<String, String> getFilters(String filter, Function<String, String> fieldMapper) {
        if (!filter.matches("(" + FILTER_REGEX + ")+")) {
            throw new InvalidRegexInputException("The 'filter' parameter does not match the expected format");
        }

        log.debug("Getting filter items: filter={}, regex={}", filter, FILTER_REGEX);

        Map<String, String> filterItems = new HashMap<>();

        Pattern pattern = Pattern.compile(FILTER_REGEX);
        Matcher matcher = pattern.matcher(filter);

        while (matcher.find()) {
            String field = fieldMapper.apply(matcher.group(1));
            String value = matcher.group(2);

            filterItems.put(field, value);
        }

        log.info("Successfully retrieved filter items from {}", filter);

        return filterItems;
    }

    public List<Sort.Order> getDocumentSortOrders(String sort) {
        return getSortOrders(sort, DOCUMENT_SORT_REGEX, DocumentMapper::getMappedDocumentField);
    }

    public List<Sort.Order> getRevisionSortOrders(String sort) {
        return getSortOrders(sort, REVISION_SORT_REGEX, RevisionMapper::getMappedRevisionField);
    }

    private List<Sort.Order> getSortOrders(String sort, String regex, Function<String, String> fieldMapper) {
        if (!sort.matches("(" + regex + ")+")) {
            throw new InvalidRegexInputException("The 'sort' parameter does not match the expected format");
        }

        log.debug("Getting orders: sort={}, regex={}", sort, regex);

        List<Sort.Order> orders = new ArrayList<>();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sort);

        while (matcher.find()) {
            String field = fieldMapper.apply(matcher.group(1));
            Sort.Direction direction = Sort.Direction.fromString(matcher.group(2));

            orders.add(new Sort.Order(direction, field));
        }

        log.info("Successfully retrieved sort orders from {}", sort);

        return orders;
    }

    public String storeBlob(MultipartFile file) {
        return blobStorageService.storeBlob(file);
    }

    public Resource getBlob(String hash) {
        return blobStorageService.getBlob(hash);
    }

    public String getContentLength(Resource resource) {
        try {
            return String.valueOf(resource.contentLength());
        } catch (IOException e) {
            throw new FileOperationException(FileOperation.READ, "Content length could not be retrieved");
        }
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
