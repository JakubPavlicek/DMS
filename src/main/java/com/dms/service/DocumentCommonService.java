package com.dms.service;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.filter.DocumentFilter;
import com.dms.filter.RevisionFilter;
import com.dms.mapper.DocumentMapper;
import com.dms.mapper.RevisionMapper;
import com.dms.sort.DocumentSort;
import com.dms.filter.FilterItem;
import com.dms.sort.RevisionSort;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.InvalidRegexInputException;
import com.dms.exception.RevisionNotFoundException;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DocumentCommonService {

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository revisionRepository;

    private final BlobStorageService blobStorageService;
    private final ModelMapper modelMapper;

    public Document getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                                 .orElseThrow(() -> new DocumentNotFoundException("Soubor s id: " + documentId + " nebyl nalezen"));
    }

    public Document getDocument(UUID documentId, Long version) {
        Document document = getDocument(documentId);

        DocumentRevision revision = revisionRepository.findByDocumentAndVersion(document, version)
                                                      .orElseThrow(() -> new DocumentNotFoundException("Soubor s verzi: " + version + " neexistuje"));

        return copyRevisionInfoToDocument(revision, document);
    }

    public DocumentRevision getRevision(UUID revisionId) {
        return revisionRepository.findByRevisionId(revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revize s ID: " + revisionId + " nebyla nalezena"));
    }

    public DocumentRevision getRevisionByDocumentAndVersion(Document document, Long version) {
        return revisionRepository.findByDocumentAndVersion(document, version)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revize s verzi: " + version + " nebyla nalezena"));
    }

    public DocumentRevision getRevisionByDocumentAndId(Document document, UUID revisionId) {
        return revisionRepository.findByDocumentAndRevisionId(document, revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revize s ID: " + revisionId + " nebyla nalezena"));
    }

    public Page<DocumentRevision> getRevisionsBySpecification(Specification<DocumentRevision> specification, Pageable pageable) {
        return revisionRepository.findAll(specification, pageable);
    }

    public Document copyRevisionInfoToDocument(DocumentRevision revision, Document document) {
        BeanUtils.copyProperties(revision, document, "createdAt");
        document.setUpdatedAt(document.getUpdatedAt());

        return document;
    }

    public Document updateDocumentToRevision(Document document, DocumentRevision documentRevision) {
        document.setName(documentRevision.getName());
        document.setType(documentRevision.getType());
        document.setPath(documentRevision.getPath());
        document.setHash(documentRevision.getHash());
        document.setAuthor(documentRevision.getAuthor());

        // flush to immediately initialize the "createdAt" and "updatedAt" fields
        return documentRepository.saveAndFlush(document);
    }

    public void saveRevisionFromDocument(Document document) {
        DocumentRevision documentRevision = DocumentRevision.builder()
                                                            .document(document)
                                                            .version(getLastRevisionVersion(document) + 1)
                                                            .name(document.getName())
                                                            .type(document.getType())
                                                            .path(document.getPath())
                                                            .author(document.getAuthor())
                                                            .hash(document.getHash())
                                                            .build();
        revisionRepository.save(documentRevision);
    }

    public Long getLastRevisionVersion(Document document) {
        return revisionRepository.findLastRevisionVersionByDocument(document)
                                 .orElse(0L);
    }

    public void updateRevisionVersionsForDocument(Document document) {
        List<DocumentRevision> documentRevisions = revisionRepository.findAllByDocumentOrderByCreatedAtAsc(document);

        Long version = 1L;
        for (DocumentRevision revision : documentRevisions) {
            revisionRepository.updateVersion(revision, version);
            version++;
        }
    }

    public Page<Long> getDocumentVersions(Document document, Pageable pageable) {
        Page<DocumentRevision> revisions = revisionRepository.findAllByDocument(document, pageable);

        List<Long> versions = revisions.stream()
                                       .map(DocumentRevision::getVersion)
                                       .toList();

        return new PageImpl<>(versions, pageable, revisions.getTotalElements());
    }

    public List<FilterItem> getDocumentFilterItemsFromFilter(String filter) {
        if (!filter.matches("(" + DocumentFilter.FILTER_REGEX + ")+"))
            throw new InvalidRegexInputException("The 'filter' parameter does not match the expected format");

        List<FilterItem> filterItems = new ArrayList<>();

        Pattern pattern = Pattern.compile(DocumentFilter.FILTER_REGEX);
        Matcher matcher = pattern.matcher(filter);

        while (matcher.find()) {
            String field = DocumentMapper.getMappedDocumentField(matcher.group(1));
            String value = matcher.group(2);

            filterItems.add(new FilterItem(field, value));
        }

        return filterItems;
    }

    public List<FilterItem> getRevisionFilterItemsFromFilter(String filter) {
        if (!filter.matches("(" + RevisionFilter.FILTER_REGEX + ")+"))
            throw new InvalidRegexInputException("The 'filter' parameter does not match the expected format");

        List<FilterItem> filterItems = new ArrayList<>();

        Pattern pattern = Pattern.compile(RevisionFilter.FILTER_REGEX);
        Matcher matcher = pattern.matcher(filter);

        while (matcher.find()) {
            String field = RevisionMapper.getMappedRevisionField(matcher.group(1));
            String value = matcher.group(2);

            filterItems.add(new FilterItem(field, value));
        }

        return filterItems;
    }

    public List<Sort.Order> getOrdersFromDocumentSort(String sort) {
        if (!sort.matches("(" + DocumentSort.DOCUMENT_SORT_REGEX + ")+"))
            throw new InvalidRegexInputException("The 'sort' parameter does not match the expected format");

        List<Sort.Order> orders = new ArrayList<>();

        Pattern pattern = Pattern.compile(DocumentSort.DOCUMENT_SORT_REGEX);
        Matcher matcher = pattern.matcher(sort);

        while (matcher.find()) {
            String documentField = DocumentMapper.getMappedDocumentField(matcher.group(1));
            Sort.Direction direction = Sort.Direction.fromString(matcher.group(2));

            orders.add(new Sort.Order(direction, documentField));
        }

        return orders;
    }

    public List<Sort.Order> getOrdersFromRevisionSort(String sort) {
        if (!sort.matches("(" + RevisionSort.REVISION_SORT_REGEX + ")+"))
            throw new InvalidRegexInputException("The 'sort' parameter does not match the expected format");

        List<Sort.Order> orders = new ArrayList<>();

        Pattern pattern = Pattern.compile(RevisionSort.REVISION_SORT_REGEX);
        Matcher matcher = pattern.matcher(sort);

        while (matcher.find()) {
            String revisionField = RevisionMapper.getMappedRevisionField(matcher.group(1));
            Sort.Direction direction = Sort.Direction.fromString(matcher.group(2));

            orders.add(new Sort.Order(direction, revisionField));
        }

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

    public DocumentDTO mapDocumentToDocumentDto(Document document) {
        return modelMapper.map(document, DocumentDTO.class);
    }

    public DocumentRevisionDTO mapRevisionToRevisionDto(DocumentRevision revision) {
        return modelMapper.map(revision, DocumentRevisionDTO.class);
    }

}
