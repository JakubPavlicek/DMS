package com.dms.service;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.InvalidRegexInputException;
import com.dms.exception.RevisionNotFoundException;
import com.dms.filter.DocumentFilter;
import com.dms.filter.FilterItem;
import com.dms.filter.RevisionFilter;
import com.dms.mapper.dto.DocumentRevisionDTOMapper;
import com.dms.mapper.entity.DocumentMapper;
import com.dms.mapper.entity.RevisionMapper;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.sort.DocumentSort;
import com.dms.sort.RevisionSort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
@RequiredArgsConstructor
public class DocumentCommonService {

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository revisionRepository;

    private final BlobStorageService blobStorageService;

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

    public Page<DocumentRevision> getAllRevisions(Document document, Pageable pageable) {
        return revisionRepository.findAllByDocument(document, pageable);
    }

    public DocumentRevision getRevisionByDocumentAndVersion(Document document, Long version) {
        log.debug("Getting revision by document and version: documentId={}, version={}", document.getDocumentId(), version);
        return revisionRepository.findByDocumentAndVersion(document, version)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revision with version: " + version + " not found for document with ID: " + document.getDocumentId()));
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
        document.setPath(revision.getPath());
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
                                                            .path(document.getPath())
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

    public List<FilterItem> getDocumentFilterItems(String filter) {
        return getFilterItems(filter, DocumentFilter.FILTER_REGEX, DocumentMapper::getMappedDocumentField);
    }

    public List<FilterItem> getRevisionFilterItems(String filter) {
        return getFilterItems(filter, RevisionFilter.FILTER_REGEX, RevisionMapper::getMappedRevisionField);
    }

    private List<FilterItem> getFilterItems(String filter, String regex, Function<String, String> fieldMapper) {
        if (!filter.matches("(" + regex + ")+"))
            throw new InvalidRegexInputException("The 'filter' parameter does not match the expected format");

        log.debug("Getting filter items: filter={}, regex={}", filter, regex);

        List<FilterItem> filterItems = new ArrayList<>();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(filter);

        while (matcher.find()) {
            String field = fieldMapper.apply(matcher.group(1));
            String value = matcher.group(2);

            filterItems.add(new FilterItem(field, value));
        }

        log.info("Successfully retrieved filter items from {}", filter);

        return filterItems;
    }

    public List<Sort.Order> getDocumentOrders(String sort) {
        return getOrders(sort, DocumentSort.DOCUMENT_SORT_REGEX, DocumentMapper::getMappedDocumentField);
    }

    public List<Sort.Order> getRevisionOrders(String sort) {
        return getOrders(sort, RevisionSort.REVISION_SORT_REGEX, RevisionMapper::getMappedRevisionField);
    }

    private List<Sort.Order> getOrders(String sort, String regex, Function<String, String> fieldMapper) {
        if (!sort.matches("(" + regex + ")+"))
            throw new InvalidRegexInputException("The 'sort' parameter does not match the expected format");

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

    public byte[] getBlob(String hash) {
        return blobStorageService.getBlob(hash);
    }

    public void deleteBlobIfDuplicateHashNotExists(String hash) {
        if (!isDuplicateHashPresent(hash))
            blobStorageService.deleteBlob(hash);
    }

    private boolean isDuplicateHashPresent(String hash) {
        return documentRepository.duplicateHashExists(hash) || revisionRepository.duplicateHashExists(hash);
    }

    public boolean pathWithFileAlreadyExists(String path, String filename, User user) {
        return documentRepository.pathWithFileAlreadyExists(path, filename, user) || revisionRepository.pathWithFileAlreadyExists(path, filename, user);
    }

}
