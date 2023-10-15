package com.dms.service;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.FilterItem;
import com.dms.dto.SortItem;
import com.dms.dto.UserDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DocumentCommonService {

    @Value("${regex.sort}")
    private String sortRegex;

    @Value("${regex.sort-pattern}")
    private String sortRegexPattern;

    @Value("${regex.filter}")
    private String filterRegex;

    @Value("${regex.filter-pattern}")
    private String filterRegexPattern;

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository revisionRepository;

    private final BlobStorageService blobStorageService;
    private final ModelMapper modelMapper;

    public Document getDocument(String documentId) {
        return documentRepository.findById(documentId)
                                 .orElseThrow(() -> new DocumentNotFoundException("Soubor s id: " + documentId + " nebyl nalezen"));
    }

    public Document getDocument(String documentId, Long version) {
        Document document = getDocument(documentId);

        DocumentRevision revision = revisionRepository.findByDocumentAndVersion(document, version)
                                                      .orElseThrow(() -> new DocumentNotFoundException("Soubor s verzi: " + version + " neexistuje"));

        return copyRevisionInfoToDocument(revision, document);
    }

    public DocumentRevision getRevision(Long revisionId) {
        return revisionRepository.findByRevisionId(revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revize s ID: " + revisionId + " nebyla nalezena"));
    }

    public DocumentRevision getRevisionByDocumentAndVersion(Document document, Long version) {
        return revisionRepository.findByDocumentAndVersion(document, version)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revize s verzi: " + version + " nebyla nalezena"));
    }

    public DocumentRevision getRevisionByDocumentAndId(Document document, Long revisionId) {
        return revisionRepository.findByDocumentAndRevisionId(document, revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revize s ID: " + revisionId + " nebyla nalezena"));
    }

    public Page<DocumentRevision> getRevisionsBySpecification(Specification<DocumentRevision> specification, Pageable pageable) {
        return revisionRepository.findAll(specification, pageable);
    }

    public Page<DocumentRevision> getRevisionsByDocument(Document document, Pageable pageable) {
        return revisionRepository.findAllByDocument(document, pageable);
    }

    public Document copyRevisionInfoToDocument(DocumentRevision revision, Document document) {
        BeanUtils.copyProperties(revision, document, "createdAt");
        document.setUpdatedAt(document.getUpdatedAt());

        return document;
    }

    public Document updateDocumentToRevision(Document document, DocumentRevision documentRevision) {
        document.setName(documentRevision.getName());
        document.setType(documentRevision.getType());
        document.setAuthor(documentRevision.getAuthor());
        document.setHash(documentRevision.getHash());

        // flush to immediately initialize the "createdAt" and "updatedAt" fields
        return documentRepository.saveAndFlush(document);
    }

    public void saveRevisionFromDocument(Document document) {
        DocumentRevision documentRevision = DocumentRevision.builder()
                                                            .document(document)
                                                            .version(getLastRevisionVersion(document) + 1)
                                                            .name(document.getName())
                                                            .type(document.getType())
                                                            .author(document.getAuthor())
                                                            .hash(document.getHash())
                                                            .build();
        revisionRepository.save(documentRevision);
    }

    private Long getLastRevisionVersion(Document document) {
        return revisionRepository.findLastRevisionVersionByDocument(document)
                                 .orElse(0L);
    }

    private Sort getSortFromFields(List<SortItem> sortItems) {
        List<Sort.Order> orders = new ArrayList<>();

        for (SortItem sortItem : sortItems) {
            String field = sortItem.getField();
            Sort.Direction direction = sortItem.getDirection();

            orders.add(new Sort.Order(direction, field));
        }

        return Sort.by(orders);
    }

    public Pageable createPageable(int pageNumber, int pageSize, List<SortItem> sortItems) {
        Sort sort = Sort.unsorted();

        if (Objects.nonNull(sortItems))
            sort = getSortFromFields(sortItems);

        return PageRequest.of(pageNumber, pageSize, sort);
    }

    public List<SortItem> parseSortItems(String sort) {
        if (Objects.isNull(sort))
            return null;

        if (!sort.matches(sortRegexPattern))
            throw new InvalidRegexInputException("The 'sort' parameter does not match the expected format");

        List<SortItem> sortItems = new ArrayList<>();

        Pattern pattern = Pattern.compile(sortRegex);
        Matcher matcher = pattern.matcher(sort);

        while (matcher.find()) {
            String field = matcher.group(1);
            String directionStr = matcher.group(2);
            Sort.Direction direction = Sort.Direction.fromString(directionStr);

            sortItems.add(new SortItem(field, direction));
        }

        return sortItems;
    }

    public List<FilterItem> parseFilterItems(String filter) {
        if (Objects.isNull(filter))
            return null;

        if (!filter.matches(filterRegexPattern))
            throw new InvalidRegexInputException("The 'filter' parameter does not match the expected format");

        List<FilterItem> filterItems = new ArrayList<>();

        Pattern pattern = Pattern.compile(filterRegex);
        Matcher matcher = pattern.matcher(filter);

        while (matcher.find()) {
            String field = matcher.group(1);
            String value = matcher.group(2);

            filterItems.add(new FilterItem(field, value));
        }

        return filterItems;
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

    public DocumentDTO mapDocumentToDocumentDto(Document document) {
        return modelMapper.map(document, DocumentDTO.class);
    }

    public DocumentRevisionDTO mapRevisionToRevisionDto(DocumentRevision revision) {
        return modelMapper.map(revision, DocumentRevisionDTO.class);
    }

    public User mapUserDtoToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

}
