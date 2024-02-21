package com.dms.integration.controller;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.repository.UserRepository;
import com.dms.service.BlobStorageService;
import com.dms.service.DocumentService;
import com.dms.util.JwtManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class DocumentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentRevisionRepository revisionRepository;

    @Autowired
    private BlobStorageService blobStorageService;

    private MockMultipartFile firstFile;
    private MockMultipartFile secondFile;
    private MockMultipartFile thirdFile;

    private String firstHash;
    private String secondHash;
    private String thirdHash;

    private User author;
    private Document document;
    private DocumentRevision currentRevision;
    private DocumentRevision secondRevision;
    private DocumentRevision thirdRevision;

    @BeforeEach
    void setUp() {
        firstFile = new MockMultipartFile("first_file", "document.txt", MediaType.TEXT_PLAIN_VALUE, "first".getBytes());
        firstHash = blobStorageService.storeBlob(firstFile);

        secondFile = new MockMultipartFile("second_file", "document.txt", MediaType.TEXT_PLAIN_VALUE, "second".getBytes());
        secondHash = blobStorageService.storeBlob(secondFile);

        thirdFile = new MockMultipartFile("third_file", "document.txt", MediaType.TEXT_PLAIN_VALUE, "third".getBytes());
        thirdHash = blobStorageService.storeBlob(thirdFile);

        author = User.builder()
                     .email("james@gmail.com")
                     .name("james")
                     .password("secret123!")
                     .build();

        document = Document.builder()
                           .author(author)
                           .version(1L)
                           .name(firstFile.getOriginalFilename())
                           .type(firstFile.getContentType())
                           .path("/")
                           .hash(firstHash)
                           .build();

        currentRevision = DocumentRevision.builder()
                                          .author(author)
                                          .document(document)
                                          .version(1L)
                                          .name(firstFile.getOriginalFilename())
                                          .type(firstFile.getContentType())
                                          .hash(firstHash)
                                          .build();

        secondRevision = DocumentRevision.builder()
                                         .author(author)
                                         .document(document)
                                         .version(2L)
                                         .name(secondFile.getOriginalFilename())
                                         .type(secondFile.getContentType())
                                         .hash(secondHash)
                                         .build();

        thirdRevision = DocumentRevision.builder()
                                        .author(author)
                                        .document(document)
                                        .version(3L)
                                        .name(thirdFile.getOriginalFilename())
                                        .type(thirdFile.getContentType())
                                        .hash(thirdHash)
                                        .build();

        author = userRepository.save(author);
        document.setRevisions(List.of(currentRevision, secondRevision, thirdRevision));
        document = documentRepository.save(document);
        currentRevision = revisionRepository.save(currentRevision);
        secondRevision = revisionRepository.save(secondRevision);
        thirdRevision = revisionRepository.save(thirdRevision);
    }

    @AfterEach
    void tearDown() {
        blobStorageService.deleteBlob(firstHash);
        blobStorageService.deleteBlob(secondHash);
        blobStorageService.deleteBlob(thirdHash);
    }

    @Test
    void shouldDeleteDocumentWithRevisions() throws Exception {
        mvc.perform(delete("/documents/{documentId}", document.getDocumentId())
               .with(jwt().jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpect(status().isNoContent());

        Optional<Document> documentById = documentRepository.findById(document.getId());
        Optional<DocumentRevision> currentRevisionById = revisionRepository.findById(currentRevision.getId());
        Optional<DocumentRevision> secondRevisionById = revisionRepository.findById(secondRevision.getId());
        Optional<DocumentRevision> thirdRevisionById = revisionRepository.findById(thirdRevision.getId());

        assertThat(documentById).isEmpty();
        assertThat(currentRevisionById).isEmpty();
        assertThat(secondRevisionById).isEmpty();
        assertThat(thirdRevisionById).isEmpty();
    }

    @Test
    void shouldNotDeleteDocumentWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(delete("/documents/{documentId}", document.getDocumentId()))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotDeleteDocumentWhenDocumentIsNotFound() throws Exception {
        mvc.perform(delete("/documents/{documentId}", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldDownloadDocument() throws Exception {
        mvc.perform(get("/documents/{documentId}/download", document.getDocumentId())
               .with(jwt().jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.TEXT_PLAIN),
               header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")),
               header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("filename")),
               header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(firstFile.getOriginalFilename())),
               header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(firstFile.getBytes().length)),
               content().bytes(firstFile.getBytes())
           );
    }

    @Test
    void shouldNotDownloadDocumentWhenDocumentIsNotFound() throws Exception {
        mvc.perform(get("/documents/{documentId}/download", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldReturnDocument() throws Exception {
        mvc.perform(get("/documents/{documentId}", document.getDocumentId())
               .with(jwt().jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.documentId").value(document.getDocumentId()),
               jsonPath("$.author.userId").value(author.getUserId()),
               jsonPath("$.version").value(document.getVersion()),
               jsonPath("$.name").value(document.getName()),
               jsonPath("$.type").value(document.getType()),
               jsonPath("$.path").value(document.getPath()),
               jsonPath("$.createdAt").isNotEmpty(),
               jsonPath("$.updatedAt").isNotEmpty()
           );
    }

    @Test
    void shouldNotReturnDocumentWhenDocumentIsNotFound() throws Exception {
        mvc.perform(get("/documents/{documentId}", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

}