package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import com.dms.exception.InvalidRegexInputException;
import com.dms.exception.RevisionNotFoundException;
import com.dms.mapper.entity.DocumentMapper;
import com.dms.mapper.entity.RevisionMapper;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
@RequiredArgsConstructor
public class DocumentCommonService {

    private static final String DOCUMENT_FILTER_REGEX = "(name|type|path|is_archived):\"([\\w\\s]*)\"(?:,|$)";
    private static final String REVISION_FILTER_REGEX = "(name|type):\"([\\w\\s]*)\"(?:,|$)";

    private static final String DOCUMENT_SORT_REGEX = "(document_id|name|type|path|version|created_at|updated_at):(asc|desc)(?:,|$)";
    private static final String REVISION_SORT_REGEX = "(revision_id|name|type|version|created_at):(asc|desc)(?:,|$)";

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository revisionRepository;

    private final BlobStorageService blobStorageService;

    public DocumentRevision getRevisionByDocumentAndId(Document document, String revisionId) {
        log.debug("Getting revision by document and ID: document={}, revisionId={}", document, revisionId);

        return revisionRepository.findByDocumentAndRevisionId(document, revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revision with ID: " + revisionId + " not found for document: " + document));
    }

    public void saveDocument(Document document) {
        documentRepository.save(document);
    }

    public Document updateDocumentToRevision(Document document, DocumentRevision revision) {
        log.debug("Updating document to revision: document={}, revision={}", document, revision);

        document.setName(revision.getName());
        document.setType(revision.getType());
        document.setHash(revision.getHash());
        document.setVersion(revision.getVersion());

        Document updatedDocument = documentRepository.save(document);

        log.info("Successfully updated details of document {} from revision {}", document, revision);

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

        log.info("Revision {} saved successfully from document {}", savedRevision, document);
    }

    public Long getLastRevisionVersion(Document document) {
        return revisionRepository.findLastRevisionVersionByDocument(document)
                                 .orElse(0L);
    }

    public Integer getRevisionCountForDocument(Document document) {
        return revisionRepository.countAllByDocument(document);
    }

    public Page<DocumentRevision> findAllRevisionsByDocument(Document document, Pageable pageable) {
        return revisionRepository.findAllByDocument(document, pageable);
    }

    public void updateRevisionVersionsForDocument(Document document) {
        log.debug("Updating revision versions for document: document={}", document);

        List<DocumentRevision> documentRevisions = revisionRepository.findAllByDocumentOrderByCreatedAtAsc(document);

        Long version = 1L;
        for (DocumentRevision revision : documentRevisions) {
            revision.setVersion(version);
            revisionRepository.save(revision);
            version++;
        }
    }

    public Page<DocumentRevision> findRevisions(Specification<DocumentRevision> specification, Pageable pageable) {
        return revisionRepository.findAll(specification, pageable);
    }

    public Map<String, String> getDocumentFilters(String filter) {
        return getFilters(filter, DOCUMENT_FILTER_REGEX, DocumentMapper::getMappedDocumentField);
    }

    public Map<String, String> getRevisionFilters(String filter) {
        return getFilters(filter, REVISION_FILTER_REGEX, RevisionMapper::getMappedRevisionField);
    }

    private Map<String, String> getFilters(String filter, String regex, UnaryOperator<String> fieldMapper) {
        if (!filter.matches("(" + regex + ")+")) {
            throw new InvalidRegexInputException("The 'filter' parameter does not match the expected format");
        }

        log.debug("Getting filter items: filter={}, regex={}", filter, regex);

        Map<String, String> filterItems = new HashMap<>();

        Pattern pattern = Pattern.compile(regex);
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

    private List<Sort.Order> getSortOrders(String sort, String regex, UnaryOperator<String> fieldMapper) {
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
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.FILE);
        }
    }

    public void safelyDeleteBlob(String hash) {
        if (!isHashDuplicate(hash)) {
            blobStorageService.deleteBlob(hash);
        }
    }

    private boolean isHashDuplicate(String hash) {
        return documentRepository.duplicateHashExists(hash) || revisionRepository.duplicateHashExists(hash);
    }

}
