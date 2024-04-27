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

/**
 * Service class for common document and document revision-related operations.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class DocumentCommonService {

    /** Regular expression pattern for document filters. */
    private static final String DOCUMENT_FILTER_REGEX = "(name|type|path|is_archived):\"([\\w\\s]*)\"(?:,|$)";
    /** Regular expression pattern for revision filters. */
    private static final String REVISION_FILTER_REGEX = "(name|type):\"([\\w\\s]*)\"(?:,|$)";

    /** Regular expression pattern for document sorting. */
    private static final String DOCUMENT_SORT_REGEX = "(document_id|name|type|path|size|version|created_at|updated_at):(asc|desc)(?:,|$)";
    /** Regular expression pattern for revision sorting. */
    private static final String REVISION_SORT_REGEX = "(revision_id|name|type|size|version|created_at):(asc|desc)(?:,|$)";

    /** Repository for managing documents. */
    private final DocumentRepository documentRepository;
    /** Repository for managing document revisions. */
    private final DocumentRevisionRepository revisionRepository;

    /** Service for managing blob storage. */
    private final BlobStorageService blobStorageService;

    /**
     * Retrieves a document revision by document and revision ID.
     *
     * @param document the document to which the revision belongs
     * @param revisionId the ID of the revision to retrieve
     * @return the document revision
     * @throws RevisionNotFoundException if the revision with the given ID is not found for the document
     */
    public DocumentRevision getRevisionByDocumentAndId(Document document, String revisionId) {
        log.debug("Getting revision by document and ID: document={}, revisionId={}", document, revisionId);

        return revisionRepository.findByDocumentAndRevisionId(document, revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revision with ID: " + revisionId + " not found for document: " + document));
    }

    /**
     * Saves a document.
     *
     * @param document the document to save
     */
    public void saveDocument(Document document) {
        documentRepository.save(document);
    }

    /**
     * Updates a document to a new revision.
     *
     * @param document the document to update
     * @param revision the new revision to update the document to
     * @return the updated document
     */
    public Document updateDocumentToRevision(Document document, DocumentRevision revision) {
        log.debug("Updating document to revision: document={}, revision={}", document, revision);

        document.setName(revision.getName());
        document.setType(revision.getType());
        document.setSize(revision.getSize());
        document.setHash(revision.getHash());
        document.setVersion(revision.getVersion());

        Document updatedDocument = documentRepository.save(document);

        log.info("Successfully updated details of document {} from revision {}", document, revision);

        return updatedDocument;
    }

    /**
     * Deletes a document revision.
     *
     * @param revision the revision to delete
     */
    public void deleteRevision(DocumentRevision revision) {
        revisionRepository.delete(revision);
    }

    /**
     * Saves a new revision from the given document.
     *
     * @param document the document from which to create the revision
     */
    public void saveRevisionFromDocument(Document document) {
        log.debug("Saving revision from document: document={}", document);

        DocumentRevision documentRevision = DocumentRevision.builder()
                                                            .document(document)
                                                            .version(getLastRevisionVersion(document) + 1)
                                                            .name(document.getName())
                                                            .type(document.getType())
                                                            .size(document.getSize())
                                                            .author(document.getAuthor())
                                                            .hash(document.getHash())
                                                            .build();

        DocumentRevision savedRevision = revisionRepository.save(documentRevision);

        log.info("Revision {} saved successfully from document {}", savedRevision, document);
    }

    /**
     * Retrieves the last revision version for the given document.
     *
     * @param document the document for which to retrieve the last revision version
     * @return the last revision version, or 0 if no revisions exist for the document
     */
    public Long getLastRevisionVersion(Document document) {
        return revisionRepository.findLastRevisionVersionByDocument(document)
                                 .orElse(0L);
    }

    /**
     * Retrieves the count of revisions for the given document.
     *
     * @param document the document for which to count the revisions
     * @return the count of revisions
     */
    public Integer getRevisionCountForDocument(Document document) {
        return revisionRepository.countAllByDocument(document);
    }

    /**
     * Retrieves all revisions of the given document.
     *
     * @param document the document for which to retrieve revisions
     * @param pageable the pagination information
     * @return a page of document revisions
     */
    public Page<DocumentRevision> findAllRevisionsByDocument(Document document, Pageable pageable) {
        return revisionRepository.findAllByDocument(document, pageable);
    }

    /**
     * Updates the revision versions for the given document.
     *
     * @param document the document for which to update revision versions
     */
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

    /**
     * Finds revisions based on the given specification and pageable information.
     *
     * @param specification the specification to filter revisions
     * @param pageable the pagination information
     * @return a page of document revisions
     */
    public Page<DocumentRevision> findRevisions(Specification<DocumentRevision> specification, Pageable pageable) {
        return revisionRepository.findAll(specification, pageable);
    }

    /**
     * Retrieves document filters based on the given filter string.
     *
     * @param filter the filter string to parse
     * @return a map of document filters
     */
    public Map<String, String> getDocumentFilters(String filter) {
        return getFilters(filter, DOCUMENT_FILTER_REGEX, DocumentMapper::getMappedDocumentField);
    }

    /**
     * Retrieves revision filters based on the given filter string.
     *
     * @param filter the filter string to parse
     * @return a map of revision filters
     */
    public Map<String, String> getRevisionFilters(String filter) {
        return getFilters(filter, REVISION_FILTER_REGEX, RevisionMapper::getMappedRevisionField);
    }

    /**
     * Retrieves filters based on the given filter string, regular expression and field mapper function.
     *
     * @param filter the filter string to parse
     * @param regex the regular expression to match the filter
     * @param fieldMapper the function to map the matched filter field
     * @return a map of filter items
     * @throws InvalidRegexInputException if the filter string does not match the expected format
     */
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

    /**
     * Retrieves sort orders for documents based on the given sort string.
     *
     * @param sort the sort string to parse
     * @return a list of sort orders for documents
     * @throws InvalidRegexInputException if the sort string does not match the expected format
     */
    public List<Sort.Order> getDocumentSortOrders(String sort) {
        return getSortOrders(sort, DOCUMENT_SORT_REGEX, DocumentMapper::getMappedDocumentField);
    }

    /**
     * Retrieves sort orders for document revisions based on the given sort string.
     *
     * @param sort the sort string to parse
     * @return a list of sort orders for document revisions
     * @throws InvalidRegexInputException if the sort string does not match the expected format
     */
    public List<Sort.Order> getRevisionSortOrders(String sort) {
        return getSortOrders(sort, REVISION_SORT_REGEX, RevisionMapper::getMappedRevisionField);
    }

    /**
     * Retrieves sort orders based on the given sort string, regular expression, and field mapper function.
     *
     * @param sort the sort string to parse
     * @param regex the regular expression to match against the sort string
     * @param fieldMapper the function to map field names according to the regex groups
     * @return a list of sort orders
     * @throws InvalidRegexInputException if the sort string does not match the expected format
     */
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

    /**
     * Stores the blob represented by the provided multipart file.
     *
     * @param file the multipart file to store
     * @return the hash of the stored blob
     * @throws FileOperationException if an error occurs while storing the blob
     */
    public String storeBlob(MultipartFile file) {
        return blobStorageService.storeBlob(file);
    }

    /**
     * Retrieves the blob resource associated with the given hash.
     *
     * @param hash the hash of the blob
     * @return the resource representing the blob
     * @throws FileOperationException if an error occurs while retrieving the blob
     */
    public Resource getBlob(String hash) {
        return blobStorageService.getBlob(hash);
    }

    /**
     * Retrieves the content length of the provided resource.
     *
     * @param resource the resource for which to retrieve the content length
     * @return the content length of the resource
     * @throws FileOperationException if an error occurs while retrieving the content length
     */
    public String getContentLength(Resource resource) {
        try {
            return String.valueOf(resource.contentLength());
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.FILE);
        }
    }

    /**
     * Safely deletes the blob associated with the given hash if it is not a duplicate.
     *
     * @param hash the hash of the blob to delete
     */
    public void safelyDeleteBlob(String hash) {
        if (!isHashDuplicate(hash)) {
            blobStorageService.deleteBlob(hash);
        }
    }

    /**
     * Checks whether the given hash is a duplicate in either the document or revision repositories.
     *
     * @param hash the hash to check
     * @return true if the hash is a duplicate, false otherwise
     */
    private boolean isHashDuplicate(String hash) {
        return documentRepository.duplicateHashExists(hash) || revisionRepository.duplicateHashExists(hash);
    }

}
