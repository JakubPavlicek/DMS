package com.dms.service;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithDocumentsDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.InvalidRegexInputException;
import com.dms.mapper.dto.DocumentDTOMapper;
import com.dms.mapper.dto.DocumentRevisionDTOMapper;
import com.dms.mapper.dto.PageWithDocumentsDTOMapper;
import com.dms.mapper.dto.PageWithRevisionsDTOMapper;
import com.dms.mapper.entity.DocumentMapper;
import com.dms.mapper.entity.RevisionMapper;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.specification.DocumentFilterSpecification;
import com.dms.specification.RevisionFilterSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Log4j2
public class RetrievalService {

    private static final String FILTER_REGEX = "(name|type|path):\"([^,]*)\"(?:,|$)";

    private static final String DOCUMENT_SORT_REGEX = "(document_id|name|type|path|version|created_at|updated_at):(asc|desc)(?:,|$)";
    private static final String REVISION_SORT_REGEX = "(revision_id|name|type|version|created_at):(asc|desc)(?:,|$)";

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository revisionRepository;

    private final ManagementService managementService;

    public DocumentDTO getDocument(String documentId) {
        log.debug("Request - Getting document: documentId={}", documentId);

        Document document = managementService.getDocument(documentId);

        log.info("Document {} retrieved successfully", documentId);

        return DocumentDTOMapper.map(document);
    }

    public PageWithDocumentsDTO getDocuments(int pageNumber, int pageSize, String sort, String filter) {
        log.debug("Request - Listing documents: pageNumber={}, pageSize={}, sort={}, filter={}", pageNumber, pageSize, sort, filter);

        User user = managementService.getAuthenticatedUser();

        List<Sort.Order> sortOrders = getDocumentSortOrders(sort);
        Map<String, String> filters = getDocumentFilters(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<Document> specification = DocumentFilterSpecification.filter(filters, user);

        Page<DocumentDTO> documentDTOs = findDocuments(specification, pageable);

        log.info("Documents listed successfully");

        return PageWithDocumentsDTOMapper.map(documentDTOs);
    }

    private Page<DocumentDTO> findDocuments(Specification<Document> specification, Pageable pageable) {
        Page<Document> documents = documentRepository.findAll(specification, pageable);
        List<DocumentDTO> documentDTOs = documents.stream()
                                                  .map(DocumentDTOMapper::map)
                                                  .toList();

        return new PageImpl<>(documentDTOs, pageable, documents.getTotalElements());
    }

    public PageWithRevisionsDTO getDocumentRevisions(String documentId, int pageNumber, int pageSize, String sort, String filter) {
        log.debug("Request - Listing document revisions: documentId={} pageNumber={}, pageSize={}, sort={}, filter={}", documentId, pageNumber, pageSize, sort, filter);

        Document document = managementService.getDocument(documentId);
        User user = managementService.getAuthenticatedUser();

        List<Sort.Order> sortOrders = getRevisionSortOrders(sort);
        Map<String, String> filters = getRevisionFilters(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<DocumentRevision> specification = RevisionFilterSpecification.filterByDocument(document, filters, user);

        Page<DocumentRevisionDTO> documentRevisionDTOs = findRevisions(specification, pageable);

        log.info("Document revisions listed successfully");

        return PageWithRevisionsDTOMapper.map(documentRevisionDTOs);
    }

    public DocumentRevisionDTO getRevision(String revisionId) {
        log.debug("Request - Getting revision: revisionId={}", revisionId);

        DocumentRevision revision = managementService.getRevision(revisionId);

        log.info("Revision {} retrieved successfully", revisionId);

        return DocumentRevisionDTOMapper.map(revision);
    }

    public PageWithRevisionsDTO getRevisions(int pageNumber, int pageSize, String sort, String filter) {
        log.debug("Request - Listing revisisons: pageNumber={}, pageSize={}, sort={}, filter={}", pageNumber, pageSize, sort, filter);

        User user = managementService.getAuthenticatedUser();

        List<Sort.Order> sortOrders = getRevisionSortOrders(sort);
        Map<String, String> filters = getRevisionFilters(filter);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<DocumentRevision> specification = RevisionFilterSpecification.filter(filters, user);

        Page<DocumentRevisionDTO> documentRevisionDTOs = findRevisions(specification, pageable);

        log.info("Revisions listed successfully");

        return PageWithRevisionsDTOMapper.map(documentRevisionDTOs);
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

}
