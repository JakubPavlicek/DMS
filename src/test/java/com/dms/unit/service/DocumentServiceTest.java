package com.dms.unit.service;

import com.dms.dto.DestinationDTO;
import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithDocumentsDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.Document_;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.FileWithPathAlreadyExistsException;
import com.dms.repository.DocumentRepository;
import com.dms.service.DocumentCommonService;
import com.dms.service.DocumentService;
import com.dms.service.UserService;
import com.dms.specification.DocumentFilterSpecification;
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
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentCommonService documentCommonService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DocumentService documentService;

    private User author;
    private Document document;

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
    }

    @Test
    void shouldReturnDocument() {
        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), author)).thenReturn(Optional.of(document));

        DocumentDTO documentDTO = documentService.getDocument(document.getDocumentId());

        assertThat(documentDTO).isNotNull();
        assertThat(documentDTO.getVersion()).isEqualTo(document.getVersion());
        assertThat(documentDTO.getName()).isEqualTo(document.getName());
        assertThat(documentDTO.getType()).isEqualTo(document.getType());
        assertThat(documentDTO.getPath()).isEqualTo(document.getPath());
    }

    @Test
    void shouldThrowDocumentNotFoundExceptionWhenDocumentDoesNotExist() {
        String documentId = "d1246d35-3f46-4c57-b037-f9466c313ec3";

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.findByDocumentIdAndAuthor(documentId, author)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocument(documentId)).isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void shouldUploadDocument() {
        Document savedDocument = Document.builder()
                                         .id(1L)
                                         .author(author)
                                         .documentId("5d04e268-9c59-445f-a94b-a4f579fa12a3")
                                         .version(1L)
                                         .name("document.txt")
                                         .type("text/plain")
                                         .path("/home")
                                         .hash("185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969")
                                         .build();

        MockMultipartFile file = new MockMultipartFile("document.txt", "some text".getBytes());
        DestinationDTO destination = new DestinationDTO("/home");

        when(documentCommonService.storeBlob(file)).thenReturn(document.getHash());
        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.documentWithPathAlreadyExists(anyString(), anyString(), any(User.class))).thenReturn(false);
        when(documentRepository.saveAndFlush(any(Document.class))).thenReturn(savedDocument);

        DocumentDTO documentDTO = documentService.uploadDocument(file, destination);

        assertThat(documentDTO).isNotNull();
        assertThat(documentDTO.getDocumentId()).isEqualTo(savedDocument.getDocumentId());
        assertThat(documentDTO.getPath()).isEqualTo(savedDocument.getPath());

        verify(documentCommonService, times(1)).storeBlob(any(MultipartFile.class));
        verify(userService, times(2)).getAuthenticatedUser();
        verify(documentRepository, times(1)).documentWithPathAlreadyExists(anyString(), anyString(), any(User.class));
        verify(documentRepository, times(1)).saveAndFlush(any(Document.class));
        verify(documentCommonService, times(1)).saveRevisionFromDocument(any(Document.class));
    }

    @Test
    void shouldNotUploadDocumentWhenPathIsNotUnique() {
        MockMultipartFile file = new MockMultipartFile("document.txt", "some text".getBytes());
        DestinationDTO destination = new DestinationDTO("/home");

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.documentWithPathAlreadyExists(anyString(), anyString(), any(User.class))).thenReturn(true);

        assertThatThrownBy(() -> documentService.uploadDocument(file, destination)).isInstanceOf(FileWithPathAlreadyExistsException.class);

        verify(documentCommonService, never()).storeBlob(any(MultipartFile.class));
        verify(documentRepository, never()).saveAndFlush(any(Document.class));
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void shouldUploadNewDocumentVersion() {
        Document savedDocument = Document.builder()
                                         .id(1L)
                                         .author(author)
                                         .documentId("5d04e268-9c59-445f-a94b-a4f579fa12a3")
                                         .version(2L)
                                         .name("document.txt")
                                         .type("text/plain")
                                         .path("/home")
                                         .hash("185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969")
                                         .build();

        MockMultipartFile file = new MockMultipartFile("document.txt", "some text".getBytes());
        DestinationDTO destination = new DestinationDTO("/home");

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), author)).thenReturn(Optional.of(document));
        when(documentRepository.documentWithPathAlreadyExists(anyString(), anyString(), any(User.class))).thenReturn(false);
        when(documentCommonService.storeBlob(file)).thenReturn(document.getHash());
        when(documentCommonService.getLastRevisionVersion(any(Document.class))).thenReturn(1L);
        when(documentRepository.saveAndFlush(any(Document.class))).thenReturn(savedDocument);

        DocumentDTO documentDTO = documentService.uploadNewDocumentVersion(document.getDocumentId(), file, destination);

        assertThat(documentDTO).isNotNull();
        assertThat(documentDTO.getDocumentId()).isEqualTo(savedDocument.getDocumentId());
        assertThat(documentDTO.getVersion()).isEqualTo(savedDocument.getVersion());
        assertThat(documentDTO.getPath()).isEqualTo(savedDocument.getPath());

        verify(documentRepository, times(1)).documentWithPathAlreadyExists(anyString(), anyString(), any(User.class));
        verify(documentRepository, times(1)).saveAndFlush(any(Document.class));
    }

    @Test
    void shouldUploadNewDocumentVersionWithOldPathWhenDestinationIsNull() {
        Document savedDocument = Document.builder()
                                         .id(1L)
                                         .author(author)
                                         .documentId("5d04e268-9c59-445f-a94b-a4f579fa12a3")
                                         .version(2L)
                                         .name("document.txt")
                                         .type("text/plain")
                                         .path("/old_path")
                                         .hash("185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969")
                                         .build();

        MockMultipartFile file = new MockMultipartFile("document.txt", "some text".getBytes());

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), author)).thenReturn(Optional.of(document));
        when(documentCommonService.storeBlob(file)).thenReturn(document.getHash());
        when(documentCommonService.getLastRevisionVersion(any(Document.class))).thenReturn(1L);
        when(documentRepository.saveAndFlush(any(Document.class))).thenReturn(savedDocument);

        DocumentDTO documentDTO = documentService.uploadNewDocumentVersion(document.getDocumentId(), file, null);

        assertThat(documentDTO).isNotNull();
        assertThat(documentDTO.getDocumentId()).isEqualTo(savedDocument.getDocumentId());
        assertThat(documentDTO.getVersion()).isEqualTo(savedDocument.getVersion());
        assertThat(documentDTO.getPath()).isEqualTo(savedDocument.getPath());

        verify(documentRepository, never()).documentWithPathAlreadyExists(anyString(), anyString(), any(User.class));
        verify(documentRepository, times(1)).saveAndFlush(any(Document.class));
    }

    @Test
    void shouldSwitchDocumentToRevision() {
        DocumentRevision revision = DocumentRevision.builder()
                                                    .id(1L)
                                                    .author(author)
                                                    .document(document)
                                                    .revisionId("ee1682e7-86fd-43a0-9546-9bff0acc318f")
                                                    .version(1L)
                                                    .name("new_document.txt")
                                                    .type("text/plain")
                                                    .hash("5a49eb92b351b9c1812ade678bd12e1a38583772733ee41e8607d3d830426855")
                                                    .build();

        Document savedDocument = Document.builder()
                                         .id(1L)
                                         .author(revision.getAuthor())
                                         .documentId(document.getDocumentId())
                                         .version(revision.getVersion())
                                         .name(revision.getName())
                                         .type(revision.getType())
                                         .path(document.getPath())
                                         .hash(revision.getHash())
                                         .build();

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), author)).thenReturn(Optional.of(document));
        when(documentCommonService.getRevisionByDocumentAndId(document, revision.getRevisionId())).thenReturn(revision);
        when(documentCommonService.updateDocumentToRevision(document, revision)).thenReturn(savedDocument);

        DocumentDTO documentDTO = documentService.switchToRevision(document.getDocumentId(), revision.getRevisionId());

        assertThat(documentDTO).isNotNull();
        assertThat(documentDTO.getVersion()).isEqualTo(savedDocument.getVersion());
        assertThat(documentDTO.getName()).isEqualTo(savedDocument.getName());
        assertThat(documentDTO.getType()).isEqualTo(savedDocument.getType());
        assertThat(documentDTO.getPath()).isEqualTo(savedDocument.getPath());

        verify(documentCommonService, times(1)).updateDocumentToRevision(any(Document.class), any(DocumentRevision.class));
    }

    @Test
    void shouldDeleteDocumentWithRevisions() {
        List<DocumentRevision> revisions = List.of(new DocumentRevision(), new DocumentRevision());
        document.setRevisions(revisions);

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), author)).thenReturn(Optional.of(document));

        documentService.deleteDocumentWithRevisions(document.getDocumentId());

        verify(documentCommonService, times(2)).deleteBlobIfHashIsNotADuplicate(any());
        verify(documentRepository, times(1)).delete(any(Document.class));
    }

    @Test
    void shouldDownloadDocument() throws IOException {
        Resource resource = new ClassPathResource("example.txt");
        document.setName(resource.getFilename());

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), author)).thenReturn(Optional.of(document));
        when(documentCommonService.getBlob(anyString())).thenReturn(resource);
        when(documentCommonService.getContentLength(resource)).thenReturn(String.valueOf(resource.contentLength()));

        ResponseEntity<Resource> documentDownloadResponse = documentService.downloadDocument(document.getDocumentId());

        assertThat(documentDownloadResponse).isNotNull();
        assertThat(documentDownloadResponse.getBody()).isEqualTo(resource);
        assertThat(documentDownloadResponse.getHeaders().getContentLength()).isEqualTo(resource.contentLength());
        assertThat(documentDownloadResponse.getHeaders().getContentDisposition().isAttachment()).isTrue();
        assertThat(documentDownloadResponse.getHeaders().getContentDisposition().getFilename()).isEqualTo(resource.getFilename());
    }

    @Test
    void shouldReturnDocuments() {
        int pageNumber = 0;
        int pageSize = 10;
        String sort = "name:desc";
        String filter = "name:\"doc\",type:\"app\"";

        List<Sort.Order> sortOrders = new ArrayList<>();
        sortOrders.add(new Sort.Order(Sort.Direction.DESC, Document_.NAME));

        Map<String, String> filters = new HashMap<>();
        filters.put(Document_.NAME, "doc");
        filters.put(Document_.TYPE, "app");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<Document> specification = DocumentFilterSpecification.filter(filters, author);

        MockedStatic<DocumentFilterSpecification> mockSpecification = mockStatic(DocumentFilterSpecification.class);
        mockSpecification.when(() -> DocumentFilterSpecification.filter(filters, author)).thenReturn(specification);

        List<Document> documentList = List.of(document);
        Page<Document> documentPage = new PageImpl<>(documentList, pageable, documentList.size());

        List<DocumentDTO> documentDTOList = List.of(new DocumentDTO());
        Page<DocumentDTO> documentDTOPage = new PageImpl<>(documentDTOList, pageable, documentDTOList.size());

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentCommonService.getDocumentSortOrders(sort)).thenReturn(sortOrders);
        when(documentCommonService.getDocumentFilters(filter)).thenReturn(filters);
        when(documentRepository.findAll(specification, pageable)).thenReturn(documentPage);

        PageWithDocumentsDTO pageWithDocumentsDTO = documentService.getDocuments(pageNumber, pageSize, sort, filter);

        assertThat(pageWithDocumentsDTO).isNotNull();
        assertThat(pageWithDocumentsDTO.getContent()).hasSize(documentDTOPage.getContent().size());
        assertThat(pageWithDocumentsDTO.getTotalElements()).isEqualTo(documentDTOPage.getTotalElements());
        assertThat(pageWithDocumentsDTO.getTotalPages()).isEqualTo(documentDTOPage.getTotalPages());
        assertThat(pageWithDocumentsDTO.getFirst()).isEqualTo(documentDTOPage.isFirst());

        verify(userService, times(1)).getAuthenticatedUser();
        verify(documentCommonService, times(1)).getDocumentSortOrders(any());
        verify(documentCommonService, times(1)).getDocumentFilters(anyString());
        verify(documentRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));

        mockSpecification.close();
    }

    @Test
    void shouldReturnDocumentRevisions() {
        int pageNumber = 0;
        int pageSize = 10;
        String sort = "name:desc";
        String filter = "name:\"doc\",type:\"app\"";

        List<Sort.Order> sortOrders = new ArrayList<>();
        sortOrders.add(new Sort.Order(Sort.Direction.DESC, Document_.NAME));

        Map<String, String> filters = new HashMap<>();
        filters.put(Document_.NAME, "doc");
        filters.put(Document_.TYPE, "app");

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortOrders));
        Specification<DocumentRevision> specification = RevisionFilterSpecification.filter(filters, author);

        MockedStatic<RevisionFilterSpecification> mockSpecification = mockStatic(RevisionFilterSpecification.class);
        mockSpecification.when(() -> RevisionFilterSpecification.filterByDocument(document, filters, author)).thenReturn(specification);

        List<DocumentRevisionDTO> revisionsDTOList = List.of(new DocumentRevisionDTO());
        Page<DocumentRevisionDTO> revisionDTOPage = new PageImpl<>(revisionsDTOList, pageable, revisionsDTOList.size());

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), author)).thenReturn(Optional.of(document));
        when(documentCommonService.getRevisionSortOrders(sort)).thenReturn(sortOrders);
        when(documentCommonService.getRevisionFilters(filter)).thenReturn(filters);
        when(documentCommonService.findRevisions(specification, pageable)).thenReturn(revisionDTOPage);

        PageWithRevisionsDTO pageWithRevisionsDTO = documentService.getDocumentRevisions(document.getDocumentId(), pageNumber, pageSize, sort, filter);

        assertThat(pageWithRevisionsDTO).isNotNull();
        assertThat(pageWithRevisionsDTO.getContent()).hasSize(revisionDTOPage.getContent().size());
        assertThat(pageWithRevisionsDTO.getTotalElements()).isEqualTo(revisionDTOPage.getTotalElements());
        assertThat(pageWithRevisionsDTO.getTotalPages()).isEqualTo(revisionDTOPage.getTotalPages());
        assertThat(pageWithRevisionsDTO.getFirst()).isEqualTo(revisionDTOPage.isFirst());

        verify(userService, times(1)).getAuthenticatedUser();
        verify(documentCommonService, times(1)).getRevisionSortOrders(any());
        verify(documentCommonService, times(1)).getRevisionFilters(anyString());
        verify(documentCommonService, times(1)).findRevisions(any(), any());

        mockSpecification.close();
    }

    @Test
    void shouldMoveDocument() {
        DestinationDTO destination = new DestinationDTO("/home");

        Document savedDocument = Document.builder()
                                         .id(1L)
                                         .author(author)
                                         .documentId("5d04e268-9c59-445f-a94b-a4f579fa12a3")
                                         .version(1L)
                                         .name("document.txt")
                                         .type("text/plain")
                                         .path("/home")
                                         .hash("185f8db32271fe25f561a6fc938b2e264306ec304eda518007d1764826381969")
                                         .build();

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), author)).thenReturn(Optional.of(document));
        when(documentRepository.documentWithPathAlreadyExists(anyString(), anyString(), any(User.class))).thenReturn(false);
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        DocumentDTO documentDTO = documentService.moveDocument(document.getDocumentId(), destination);

        assertThat(documentDTO).isNotNull();
        assertThat(documentDTO.getPath()).isEqualTo(savedDocument.getPath());

        verify(documentRepository, times(1)).save(any(Document.class));
        verify(documentCommonService, times(1)).saveRevisionFromDocument(any(Document.class));
    }

    @Test
    void shouldNotMoveDocumentWhenPathIsNotUnique() {
        String documentId = document.getDocumentId();
        DestinationDTO destination = new DestinationDTO("/home");

        when(userService.getAuthenticatedUser()).thenReturn(author);
        when(documentRepository.findByDocumentIdAndAuthor(documentId, author)).thenReturn(Optional.of(document));
        when(documentRepository.documentWithPathAlreadyExists(anyString(), anyString(), any(User.class))).thenReturn(true);

        assertThatThrownBy(() -> documentService.moveDocument(documentId, destination)).isInstanceOf(FileWithPathAlreadyExistsException.class);
    }

}