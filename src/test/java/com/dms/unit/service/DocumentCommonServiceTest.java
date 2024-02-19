package com.dms.unit.service;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.DocumentRevision_;
import com.dms.entity.Document_;
import com.dms.entity.User;
import com.dms.exception.FileOperationException;
import com.dms.exception.InvalidRegexInputException;
import com.dms.exception.RevisionNotFoundException;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.service.BlobStorageService;
import com.dms.service.DocumentCommonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCommonServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentRevisionRepository revisionRepository;

    @Mock
    private BlobStorageService blobStorageService;

    @InjectMocks
    private DocumentCommonService documentCommonService;

    private User author;
    private Document document;
    private DocumentRevision revision;

    @BeforeEach
    void setUp() {
        author = User.builder()
                     .userId("d4d1ccb7-f5be-4a7a-8661-a4fbdc980364")
                     .email("james@gmail.com")
                     .name("james")
                     .password("secret123!")
                     .build();

        document = Document.builder()
                           .id(1L)
                           .author(author)
                           .documentId("d1246d35-3f46-4c57-b037-f9466c313ec3")
                           .version(1L)
                           .name("document.txt")
                           .type("text/plain")
                           .path("/")
                           .hash("185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969")
                           .build();

        revision = DocumentRevision.builder()
                                   .id(1L)
                                   .author(author)
                                   .document(document)
                                   .revisionId("ee1682e7-86fd-43a0-9546-9bff0acc318f")
                                   .version(2L)
                                   .name("new_document.txt")
                                   .type("text/plain")
                                   .hash("5a49eb92b351b9c1812ade678bd12e1a38583772733ee41e8607d3d830426855")
                                   .build();
    }

    @Test
    void shouldFindRevisionByDocumentAndRevisionId() {
        when(revisionRepository.findByDocumentAndRevisionId(document, revision.getRevisionId())).thenReturn(Optional.of(revision));

        DocumentRevision actualRevision = documentCommonService.getRevisionByDocumentAndId(document, revision.getRevisionId());

        assertThat(actualRevision).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenRevisionNotFound() {
        String revisionId = revision.getRevisionId();

        when(revisionRepository.findByDocumentAndRevisionId(document, revisionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentCommonService.getRevisionByDocumentAndId(document, revisionId)).isInstanceOf(RevisionNotFoundException.class);
    }

    @Test
    void shouldThrowRevisionNotFoundExceptionWhenDocumentIsNull() {
        String revisionId = revision.getRevisionId();

        assertThatThrownBy(() -> documentCommonService.getRevisionByDocumentAndId(null, revisionId)).isInstanceOf(RevisionNotFoundException.class);
    }

    @Test
    void shouldThrowRevisionNotFoundExceptionWhenRevisionIdIsNull() {
        assertThatThrownBy(() -> documentCommonService.getRevisionByDocumentAndId(document, null)).isInstanceOf(RevisionNotFoundException.class);
    }

    @Test
    void shouldSaveDocument() {
        documentCommonService.saveDocument(document);

        verify(documentRepository, times(1)).save(document);
    }

    @Test
    void shouldUpdateDocumentToRevision() {
        Document updatedDocument = Document.builder()
                                           .id(1L)
                                           .author(author)
                                           .documentId("d1246d35-3f46-4c57-b037-f9466c313ec3")
                                           .name(revision.getName())
                                           .type(revision.getType())
                                           .hash(revision.getHash())
                                           .version(revision.getVersion())
                                           .build();

        when(documentRepository.saveAndFlush(document)).thenReturn(updatedDocument);

        Document actualDocument = documentCommonService.updateDocumentToRevision(document, revision);

        assertThat(actualDocument).isNotNull();
        assertThat(actualDocument.getName()).isEqualTo(revision.getName());
        assertThat(actualDocument.getType()).isEqualTo(revision.getType());
        assertThat(actualDocument.getHash()).isEqualTo(revision.getHash());
        assertThat(actualDocument.getVersion()).isEqualTo(revision.getVersion());

        verify(documentRepository, times(1)).saveAndFlush(any(Document.class));
    }

    @Test
    void shouldThrowRuntimeExceptionWhenDocumentIsNullForUpdate() {
        assertThatThrownBy(() -> documentCommonService.updateDocumentToRevision(null, revision)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowRuntimeExcpetionWhenRevisionIsNull() {
        assertThatThrownBy(() -> documentCommonService.updateDocumentToRevision(document, null)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldSaveRevisionFromDocument() {
        documentCommonService.saveRevisionFromDocument(document);

        verify(revisionRepository, times(1)).save(any(DocumentRevision.class));
    }

    @Test
    void shouldThrowRuntimeExceptionWhenDocumentIsNullForRevisionSave() {
        assertThatThrownBy(() -> documentCommonService.saveRevisionFromDocument(null)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldReturnLastRevisionVersionByDocument() {
        Long expectedVersion = 1L;

        when(revisionRepository.findLastRevisionVersionByDocument(document)).thenReturn(Optional.of(expectedVersion));

        Long actualVersion = documentCommonService.getLastRevisionVersion(document);

        assertThat(actualVersion).isEqualTo(expectedVersion);
    }

    @Test
    void shouldReturnZeroWhenDocumentHasNoVersions() {
        Long expectedVersion = 0L;

        when(revisionRepository.findLastRevisionVersionByDocument(document)).thenReturn(Optional.empty());

        Long actualVersion = documentCommonService.getLastRevisionVersion(document);

        assertThat(actualVersion).isEqualTo(expectedVersion);
    }

    @Test
    void shouldUpdateRevisionVersionsForDocument() {
        DocumentRevision anotherRevision = new DocumentRevision();

        List<DocumentRevision> revisions = List.of(revision, anotherRevision);

        when(revisionRepository.findAllByDocumentOrderByCreatedAtAsc(document)).thenReturn(revisions);

        documentCommonService.updateRevisionVersionsForDocument(document);

        verify(revisionRepository, times(1)).findAllByDocumentOrderByCreatedAtAsc(any(Document.class));
        verify(revisionRepository, times(1)).save(revisions.get(0));
        verify(revisionRepository, times(1)).save(revisions.get(1));
    }

    @Test
    void shouldNotUpdateRevisionVersionsForDocumentWhenDocumentIsNull() {
        when(revisionRepository.findAllByDocumentOrderByCreatedAtAsc(null)).thenReturn(new ArrayList<>());

        documentCommonService.updateRevisionVersionsForDocument(null);

        verify(revisionRepository, times(1)).findAllByDocumentOrderByCreatedAtAsc(null);
        verify(revisionRepository, never()).save(any(DocumentRevision.class));
    }

    @Test
    void shouldFindRevisionsBySpecificationAndPageable() {
        Specification<DocumentRevision> specification = mock(Specification.class);

        List<DocumentRevision> revisions = List.of(revision);
        Pageable pageable = Pageable.unpaged();
        long totalElements = revisions.size();

        Page<DocumentRevision> pageWithRevisions = new PageImpl<>(revisions, pageable, totalElements);

        when(revisionRepository.findAll(specification, pageable)).thenReturn(pageWithRevisions);

        Page<DocumentRevisionDTO> pageWithRevisionDTOs = documentCommonService.findRevisions(specification, pageable);

        assertThat(pageWithRevisionDTOs.getTotalElements()).isEqualTo(pageWithRevisions.getTotalElements());
        assertThat(pageWithRevisionDTOs.getTotalPages()).isEqualTo(pageWithRevisions.getTotalPages());
        assertThat(pageWithRevisionDTOs.getNumber()).isEqualTo(pageWithRevisions.getNumber());
        assertThat(pageWithRevisionDTOs.getSize()).isEqualTo(pageWithRevisions.getSize());
    }

    @Test
    void shouldReturnDocumentFilters() {
        String filter = "name:\"doc\",type:\"app\"";

        Map<String, String> filters = documentCommonService.getDocumentFilters(filter);

        assertThat(filters).hasSize(2)
                           .containsEntry("name", "doc")
                           .containsEntry("type", "app");
    }

    @Test
    void shouldThrowInvalidRegexInputExceptionForInvalidDocumentFilter() {
        String filter = "some regex";

        assertThatThrownBy(() -> documentCommonService.getDocumentFilters(filter)).isInstanceOf(InvalidRegexInputException.class);
    }

    @Test
    void shouldReturnRevisionFilters() {
        String filter = "name:\"doc\",type:\"app\"";

        Map<String, String> filters = documentCommonService.getRevisionFilters(filter);

        assertThat(filters).hasSize(2)
                           .containsEntry("name", "doc")
                           .containsEntry("type", "app");
    }

    @Test
    void shouldThrowInvalidRegexInputExceptionForInvalidRevisionFilter() {
        String filter = "some regex";

        assertThatThrownBy(() -> documentCommonService.getRevisionFilters(filter)).isInstanceOf(InvalidRegexInputException.class);
    }

    @Test
    void shouldReturnDocumentSortOrders() {
        String sort = "name:desc,type:asc";

        List<Sort.Order> orders = documentCommonService.getDocumentSortOrders(sort);

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0)
                         .getProperty()).isEqualTo(Document_.NAME);
        assertThat(orders.get(0)
                         .getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(orders.get(1)
                         .getProperty()).isEqualTo(Document_.TYPE);
        assertThat(orders.get(1)
                         .getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void shouldThrowInvalidRegexInputExceptionForInvalidDocumentSort() {
        String sort = "some regex";

        assertThatThrownBy(() -> documentCommonService.getDocumentSortOrders(sort)).isInstanceOf(InvalidRegexInputException.class);
    }

    @Test
    void shouldReturnRevisionSortOrders() {
        String sort = "name:desc,type:asc";

        List<Sort.Order> orders = documentCommonService.getRevisionSortOrders(sort);

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0)
                         .getProperty()).isEqualTo(DocumentRevision_.NAME);
        assertThat(orders.get(0)
                         .getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(orders.get(1)
                         .getProperty()).isEqualTo(DocumentRevision_.TYPE);
        assertThat(orders.get(1)
                         .getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void shouldThrowInvalidRegexInputExceptionForInvalidRevisionSort() {
        String sort = "some regex";

        assertThatThrownBy(() -> documentCommonService.getRevisionSortOrders(sort)).isInstanceOf(InvalidRegexInputException.class);
    }

    @Test
    void shouldStoreBlob() {
        String expectedHash = "185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969";
        MockMultipartFile file = new MockMultipartFile("document.txt", "hello".getBytes());

        when(blobStorageService.storeBlob(file)).thenReturn(expectedHash);

        String actualHash = documentCommonService.storeBlob(file);

        assertThat(actualHash).isEqualTo(expectedHash);
    }

    @Test
    void shouldReturnBlob() {
        String hash = "185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969";
        Resource resource = new ClassPathResource("example.txt");

        when(blobStorageService.getBlob(hash)).thenReturn(resource);

        Resource actualResource = documentCommonService.getBlob(hash);

        assertThat(actualResource).isNotNull();
    }

    @Test
    void shouldReturnContentLength() throws IOException {
        Resource resource = new ClassPathResource("example.txt");

        String actualLength = documentCommonService.getContentLength(resource);

        assertThat(actualLength).isEqualTo(String.valueOf(resource.contentLength()));
    }

    @Test
    void shouldThrowFileOperationExceptionWhenResourceIsNull() {
        assertThatThrownBy(() -> documentCommonService.getContentLength(null)).isInstanceOf(FileOperationException.class);
    }

    @Test
    void shouldDeleteBlobIfHashIsNotADuplicate() {
        String hash = "185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969";

        when(documentRepository.duplicateHashExists(hash)).thenReturn(false);
        when(revisionRepository.duplicateHashExists(hash)).thenReturn(false);

        documentCommonService.deleteBlobIfHashIsNotADuplicate(hash);

        verify(blobStorageService, times(1)).deleteBlob(hash);
    }

    @Test
    void shouldNotDeleteBlobIfHashHasDuplicateInDocumentRepository() {
        String hash = "185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969";

        when(documentRepository.duplicateHashExists(hash)).thenReturn(true);

        documentCommonService.deleteBlobIfHashIsNotADuplicate(hash);

        verify(blobStorageService, never()).deleteBlob(hash);
    }

    @Test
    void shouldNotDeleteBlobIfHashHasDuplicateInRevisionRepository() {
        String hash = "185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969";

        when(documentRepository.duplicateHashExists(hash)).thenReturn(false);
        when(revisionRepository.duplicateHashExists(hash)).thenReturn(true);

        documentCommonService.deleteBlobIfHashIsNotADuplicate(hash);

        verify(blobStorageService, never()).deleteBlob(hash);
    }

}