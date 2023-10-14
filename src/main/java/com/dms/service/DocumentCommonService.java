package com.dms.service;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.SortItem;
import com.dms.dto.UserDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.RevisionNotFoundException;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
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

@Service
@AllArgsConstructor
public class DocumentCommonService {

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

        BeanUtils.copyProperties(revision, document, "createdAt");
        document.setUpdatedAt(document.getUpdatedAt());

        return document;
    }

    public DocumentRevision getRevision(Long revisionId) {
        return revisionRepository.findByRevisionId(revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revize s ID: " + revisionId + " nebyla nalezena"));
    }

    public Page<DocumentRevision> getRevisionsBySpecification(Specification<DocumentRevision> specification, Pageable pageable) {
        return revisionRepository.findAll(specification, pageable);
    }

    public Page<DocumentRevision> getRevisionsByDocument(Document document, Pageable pageable) {
        return revisionRepository.findAllByDocument(document, pageable);
    }

    public void updateDocumentToRevision(Document document, DocumentRevision documentRevision) {
        document.setName(documentRevision.getName());
        document.setType(documentRevision.getType());
        document.setAuthor(documentRevision.getAuthor());
        document.setHash(documentRevision.getHash());

        documentRepository.save(document);
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

    public DocumentRevision getRevisionByDocumentAndVersion(Document document, Long version) {
        return revisionRepository.findByDocumentAndVersion(document, version)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revize s verzi: " + version + " nebyla nalezena"));
    }

    private Sort getSortFromFields(List<SortItem> sortItems) {
        List<Sort.Order> orders = new ArrayList<>();

        for (SortItem sortItem : sortItems) {
            String field = sortItem.getField();
            String order = sortItem.getOrder();

            Sort.Direction direction = Sort.Direction.fromString(order);

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
