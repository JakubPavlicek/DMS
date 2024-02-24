package com.dms.unit.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.DocumentRevision_;
import com.dms.entity.User;
import com.dms.exception.RevisionDeletionException;
import com.dms.exception.RevisionNotFoundException;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.service.DocumentCommonService;
import com.dms.service.DocumentRevisionService;
import com.dms.service.UserService;
import com.dms.specification.RevisionFilterSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentRevisionServiceTest {

    @Mock
    private DocumentRevisionRepository revisionRepository;

    @Mock
    private DocumentCommonService documentCommonService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DocumentRevisionService documentRevisionService;

    private User author;
    private DocumentRevision revision;

    @BeforeEach
    void setUp() {
        author = User.builder()
                     .id(1L)
                     .userId("2c75c2ee-1a38-44ab-a106-8dfca58fcc7b")
                     .name("james")
                     .email("james@gmail.com")
                     .password("secret123!")
                     .build();

        Document document = Document.builder()
                                    .id(1L)
                                    .author(author)
                                    .documentId("d1246d35-3f46-4c57-b037-f9466c313ec3")
                                    .version(2L)
                                    .name("document.txt")
                                    .type("text/plain")
                                    .path("/")
                                    .hash("185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969")
                                    .build();

        revision = DocumentRevision.builder()
                                   .id(2L)
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
    void shouldReturnRevision() {
        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), author)).thenReturn(Optional.of(revision));

        DocumentRevision actualRevision = documentRevisionService.getRevision(revision.getRevisionId());

        assertThat(actualRevision).isNotNull();
        assertThat(actualRevision.getRevisionId()).isEqualTo(revision.getRevisionId());

        verify(userService, times(1)).getAuthenticatedUser();
        verify(revisionRepository, times(1)).findByRevisionIdAndAuthor(revision.getRevisionId(), author);
    }

    @Test
    void shouldThrowRevisionNotFoundExceptionWhenRevisionIsNotFound() {
        String revisionId = revision.getRevisionId();

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(revisionRepository.findByRevisionIdAndAuthor(revisionId, author)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentRevisionService.getRevision(revisionId)).isInstanceOf(RevisionNotFoundException.class);
    }

    @Test
    void shouldDeleteRevisionWithSameVersionAsDocumentAndReplaceDocumentWithPreviousRevision() {
        Document document = new Document();
        document.setVersion(2L);

        DocumentRevision revision = new DocumentRevision();
        revision.setVersion(2L);
        revision.setDocument(document);

        DocumentRevision previousRevision = new DocumentRevision();
        previousRevision.setVersion(1L);
        previousRevision.setDocument(document);

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), author)).thenReturn(Optional.of(revision));
        when(revisionRepository.findPreviousByDocumentAndVersion(document, document.getVersion())).thenReturn(Optional.of(previousRevision));

        documentRevisionService.deleteRevision(revision.getRevisionId());

        verify(revisionRepository, times(1)).findPreviousByDocumentAndVersion(document, document.getVersion());
        verify(documentCommonService, times(1)).updateDocumentToRevision(document, previousRevision);
        verify(documentCommonService, times(1)).safelyDeleteBlob(revision.getHash());
        verify(revisionRepository, times(1)).deleteByRevisionId(revision.getRevisionId());
        verify(documentCommonService, times(1)).updateRevisionVersionsForDocument(document);
        verify(documentCommonService, never()).saveDocument(document);
    }

    @Test
    void shouldDeleteRevisionWithSameVersionAsDocumentAndReplaceDocumentWithNextRevision() {
        Document document = new Document();
        document.setVersion(1L);

        DocumentRevision revision = new DocumentRevision();
        revision.setVersion(1L);
        revision.setDocument(document);

        DocumentRevision nextRevision = new DocumentRevision();
        nextRevision.setVersion(2L);
        nextRevision.setDocument(document);

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), author)).thenReturn(Optional.of(revision));
        when(revisionRepository.findPreviousByDocumentAndVersion(document, document.getVersion())).thenReturn(Optional.empty());
        when(revisionRepository.findNextByDocumentAndVersion(document, document.getVersion())).thenReturn(Optional.of(nextRevision));

        documentRevisionService.deleteRevision(revision.getRevisionId());

        verify(revisionRepository, times(1)).findPreviousByDocumentAndVersion(document, document.getVersion());
        verify(revisionRepository, times(1)).findNextByDocumentAndVersion(document, document.getVersion());
        verify(documentCommonService, times(1)).updateDocumentToRevision(document, nextRevision);
        verify(documentCommonService, times(1)).safelyDeleteBlob(revision.getHash());
        verify(revisionRepository, times(1)).deleteByRevisionId(revision.getRevisionId());
        verify(documentCommonService, times(1)).updateRevisionVersionsForDocument(document);
        verify(documentCommonService, never()).saveDocument(any(Document.class));
    }

    @Test
    void shouldThrowRevisionDeletionExceptionWhenDeletingRevisionThatIsTheOnlyVersionOfADocument() {
        Document document = new Document();
        document.setVersion(1L);

        DocumentRevision revision = new DocumentRevision();
        revision.setVersion(1L);
        revision.setDocument(document);

        String revisionId = revision.getRevisionId();

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(revisionRepository.findByRevisionIdAndAuthor(revisionId, author)).thenReturn(Optional.of(revision));
        when(revisionRepository.findPreviousByDocumentAndVersion(document, document.getVersion())).thenReturn(Optional.empty());
        when(revisionRepository.findNextByDocumentAndVersion(document, document.getVersion())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentRevisionService.deleteRevision(revisionId)).isInstanceOf(RevisionDeletionException.class);

        verify(revisionRepository, times(1)).findPreviousByDocumentAndVersion(document, document.getVersion());
        verify(revisionRepository, times(1)).findNextByDocumentAndVersion(document, document.getVersion());
        verify(documentCommonService, never()).updateDocumentToRevision(any(Document.class), any(DocumentRevision.class));
        verify(documentCommonService, never()).safelyDeleteBlob(anyString());
        verify(revisionRepository, never()).deleteByRevisionId(anyString());
        verify(documentCommonService, never()).updateRevisionVersionsForDocument(any(Document.class));
        verify(documentCommonService, never()).saveDocument(any(Document.class));
    }

    @Test
    void shouldDeleteRevisionWithLowerVersionThanDocument() {
        Document document = new Document();
        document.setVersion(2L);

        DocumentRevision revision = new DocumentRevision();
        revision.setVersion(1L);
        revision.setDocument(document);

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), author)).thenReturn(Optional.of(revision));

        documentRevisionService.deleteRevision(revision.getRevisionId());

        verify(revisionRepository, never()).findPreviousByDocumentAndVersion(any(Document.class), anyLong());
        verify(documentCommonService, never()).updateDocumentToRevision(any(Document.class), any(DocumentRevision.class));
        verify(documentCommonService, times(1)).safelyDeleteBlob(revision.getHash());
        verify(revisionRepository, times(1)).deleteByRevisionId(revision.getRevisionId());
        verify(documentCommonService, times(1)).updateRevisionVersionsForDocument(document);
        verify(documentCommonService, times(1)).saveDocument(document);
    }

    @Test
    void shouldDownloadRevision() throws IOException {
        revision.setName("example.txt");
        Resource resource = new ClassPathResource("example.txt");

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), author)).thenReturn(Optional.of(revision));
        when(documentCommonService.getBlob(revision.getHash())).thenReturn(resource);
        when(documentCommonService.getContentLength(resource)).thenReturn(String.valueOf(resource.contentLength()));

        ResponseEntity<Resource> revisionDownloadResponse = documentRevisionService.downloadRevision(revision.getRevisionId());

        assertThat(revisionDownloadResponse).isNotNull();
        assertThat(revisionDownloadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(revisionDownloadResponse.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(revisionDownloadResponse.getHeaders().getContentDisposition().isAttachment()).isTrue();
        assertThat(revisionDownloadResponse.getHeaders().getContentDisposition().getFilename()).isEqualTo(resource.getFilename());
        assertThat(revisionDownloadResponse.getHeaders().getContentLength()).isEqualTo(resource.contentLength());
        assertThat(revisionDownloadResponse.getBody()).isEqualTo(resource);
    }

    @Test
    void shouldReturnRevisions() {
        int pageNumber = 0;
        int pageSize = 10;
        String sort = "name:desc";
        String filter = "name:\"doc\",type:\"app\"";

        List<Sort.Order> sortOrders = new ArrayList<>();
        sortOrders.add(new Sort.Order(Sort.Direction.DESC, DocumentRevision_.NAME));

        Map<String, String> filters = new HashMap<>();
        filters.put(DocumentRevision_.NAME, "doc");
        filters.put(DocumentRevision_.TYPE, "app");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<DocumentRevision> specification = RevisionFilterSpecification.filter(filters, author);

        MockedStatic<RevisionFilterSpecification> mockSpecification = mockStatic(RevisionFilterSpecification.class);
        mockSpecification.when(() -> RevisionFilterSpecification.filter(filters, author)).thenReturn(specification);

        List<DocumentRevision> revisionsList = List.of(new DocumentRevision());
        Page<DocumentRevision> revisionPage = new PageImpl<>(revisionsList, pageable, revisionsList.size());

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentCommonService.getRevisionSortOrders(sort)).thenReturn(sortOrders);
        when(documentCommonService.getRevisionFilters(filter)).thenReturn(filters);
        when(documentCommonService.findRevisions(specification, pageable)).thenReturn(revisionPage);

        Page<DocumentRevision> actualRevisionPage = documentRevisionService.getRevisions(pageNumber, pageSize, sort, filter);

        assertThat(actualRevisionPage).isNotNull();
        assertThat(actualRevisionPage.getContent()).hasSize(revisionPage.getContent().size());
        assertThat(actualRevisionPage.getTotalElements()).isEqualTo(revisionPage.getTotalElements());
        assertThat(actualRevisionPage.getTotalPages()).isEqualTo(revisionPage.getTotalPages());
        assertThat(actualRevisionPage.isFirst()).isEqualTo(revisionPage.isFirst());

        verify(userService, times(1)).getAuthenticatedUser();
        verify(documentCommonService, times(1)).getRevisionSortOrders(any());
        verify(documentCommonService, times(1)).getRevisionFilters(anyString());
        verify(documentCommonService, times(1)).findRevisions(any(), any());

        mockSpecification.close();
    }

}