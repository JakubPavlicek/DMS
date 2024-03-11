package com.dms.integration.controller;

import com.dms.config.BlobStorageProperties;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.Role;
import com.dms.entity.User;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.repository.UserRepository;
import com.dms.service.BlobStorageService;
import com.dms.util.DirectoryCleaner;
import com.dms.util.JwtManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.ArrayList;
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
class DocumentRevisionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentRevisionRepository revisionRepository;

    @Autowired
    private BlobStorageService blobStorageService;

    @Autowired
    private BlobStorageProperties blobStorageProperties;

    private MockMultipartFile firstFile;
    private MockMultipartFile secondFile;
    private MockMultipartFile thirdFile;

    private String firstHash;
    private String secondHash;
    private String thirdHash;

    private User author;
    private Document document;

    private final SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + Role.USER.name());

    @BeforeEach
    void setUp() throws IOException {
        DirectoryCleaner.cleanDirectory(blobStorageProperties.getPath());

        firstFile = new MockMultipartFile("first_file", "document.txt", MediaType.TEXT_PLAIN_VALUE, "first".getBytes());
        firstHash = blobStorageService.storeBlob(firstFile);

        secondFile = new MockMultipartFile("second_file", "temp_document.txt", MediaType.TEXT_PLAIN_VALUE, "second".getBytes());
        secondHash = blobStorageService.storeBlob(secondFile);

        thirdFile = new MockMultipartFile("third_file", "final_document.txt", MediaType.TEXT_PLAIN_VALUE, "third".getBytes());
        thirdHash = blobStorageService.storeBlob(thirdFile);

        author = User.builder()
                     .email("james@gmail.com")
                     .name("james")
                     .password("secret123!")
                     .role(Role.USER)
                     .build();

        document = Document.builder()
                           .author(author)
                           .version(1L)
                           .name(firstFile.getOriginalFilename())
                           .type(firstFile.getContentType())
                           .path("/")
                           .size(firstFile.getSize())
                           .hash(firstHash)
                           .isArchived(false)
                           .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        DirectoryCleaner.cleanDirectory(blobStorageProperties.getPath());
    }

    @Test
    void shouldDeleteRevision() throws Exception {
        DocumentRevision firstRevision = DocumentRevision.builder()
                                                         .author(author)
                                                         .document(document)
                                                         .version(1L)
                                                         .name(firstFile.getOriginalFilename())
                                                         .type(firstFile.getContentType())
                                                         .size(firstFile.getSize())
                                                         .hash(firstHash)
                                                         .build();

        DocumentRevision secondRevision = DocumentRevision.builder()
                                                          .author(author)
                                                          .document(document)
                                                          .version(2L)
                                                          .name(secondFile.getOriginalFilename())
                                                          .type(secondFile.getContentType())
                                                          .size(secondFile.getSize())
                                                          .hash(secondHash)
                                                          .build();

        // mock that first revision is being deleted
        List<DocumentRevision> revisions = new ArrayList<>();
        revisions.add(secondRevision);

        document.setRevisions(revisions);

        userRepository.save(author);
        documentRepository.save(document);
        revisionRepository.save(firstRevision);
        revisionRepository.save(secondRevision);

        mvc.perform(delete("/revisions/{revisionId}", firstRevision.getRevisionId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNoContent()
           );

        Optional<Document> documentById = documentRepository.findById(document.getId());
        Optional<DocumentRevision> secondRevisionById = revisionRepository.findById(secondRevision.getId());

        assertThat(documentById).isPresent();
        assertThat(documentById.get().getVersion()).isEqualTo(1L);

        assertThat(secondRevisionById).isPresent();
        assertThat(secondRevisionById.get().getVersion()).isEqualTo(1L);
    }

    @Test
    void shouldNotDeleteRevisionWhenRevisionIsTheOnlyVersionForDocument() throws Exception {
        DocumentRevision revision = DocumentRevision.builder()
                                                    .author(author)
                                                    .document(document)
                                                    .version(1L)
                                                    .name(firstFile.getOriginalFilename())
                                                    .type(firstFile.getContentType())
                                                    .size(firstFile.getSize())
                                                    .hash(firstHash)
                                                    .build();

        List<DocumentRevision> revisions = new ArrayList<>();
        revisions.add(revision);

        document.setRevisions(revisions);

        userRepository.save(author);
        documentRepository.save(document);
        revisionRepository.save(revision);

        mvc.perform(delete("/revisions/{revisionId}", revision.getRevisionId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("Revision cannot be deleted"))
           );

        Optional<Document> documentById = documentRepository.findById(document.getId());
        Optional<DocumentRevision> revisionById = revisionRepository.findById(revision.getId());

        assertThat(documentById).isPresent();
        assertThat(documentById.get().getVersion()).isEqualTo(1L);

        assertThat(revisionById).isPresent();
        assertThat(revisionById.get().getVersion()).isEqualTo(1L);
    }

    @Test
    void shouldNotDeleteRevisionWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(delete("/revisions/{revisionId}", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotDeleteRevisionWhenRevisionIsNotFound() throws Exception {
        userRepository.save(author);

        mvc.perform(delete("/revisions/{revisionId}", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldDownloadRevision() throws Exception {
        DocumentRevision revision = DocumentRevision.builder()
                                                    .author(author)
                                                    .document(document)
                                                    .version(1L)
                                                    .name(firstFile.getOriginalFilename())
                                                    .type(firstFile.getContentType())
                                                    .size(firstFile.getSize())
                                                    .hash(firstHash)
                                                    .build();

        userRepository.save(author);
        documentRepository.save(document);
        revisionRepository.save(revision);

        mvc.perform(get("/revisions/{revisionId}/download", revision.getRevisionId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isOk(),
               header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")),
               header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("filename")),
               header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(firstFile.getOriginalFilename())),
               header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(firstFile.getSize())),
               content().bytes(firstFile.getBytes())
           );
    }

    @Test
    void shouldNotDownloadRevisionWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(get("/revisions/{revisionId}/download", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotDownloadRevisionWhenRevisionIsNotFound() throws Exception {
        mvc.perform(get("/revisions/{revisionId}/download", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldNotDownloadRevisionWhenHashDoesNotExist() throws Exception {
        DocumentRevision revision = DocumentRevision.builder()
                                                    .author(author)
                                                    .document(document)
                                                    .version(1L)
                                                    .name(firstFile.getOriginalFilename())
                                                    .type(firstFile.getContentType())
                                                    .size(firstFile.getSize())
                                                    .hash("1ef8c63124992a0beba43fc38965eab99f4333cc4b7b11425d024667a53367d9")
                                                    .build();

        userRepository.save(author);
        documentRepository.save(document);
        revisionRepository.save(revision);

        mvc.perform(get("/revisions/{revisionId}/download", revision.getRevisionId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isInternalServerError(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value("File could not be resolved")
           );
    }

    @Test
    void shouldReturnRevision() throws Exception {
        DocumentRevision revision = DocumentRevision.builder()
                                                    .author(author)
                                                    .document(document)
                                                    .version(1L)
                                                    .name(firstFile.getOriginalFilename())
                                                    .type(firstFile.getContentType())
                                                    .size(firstFile.getSize())
                                                    .hash(firstHash)
                                                    .build();

        userRepository.save(author);
        documentRepository.save(document);
        revisionRepository.save(revision);

        mvc.perform(get("/revisions/{revisionId}", revision.getRevisionId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.revisionId").value(revision.getRevisionId()),
               jsonPath("$.author.userId").value(author.getUserId()),
               jsonPath("$.version").value(revision.getVersion()),
               jsonPath("$.name").value(revision.getName()),
               jsonPath("$.type").value(revision.getType()),
               jsonPath("$.size").value(revision.getSize())
           );
    }

    @Test
    void shouldNotReturnRevisionWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(get("/revisions/{revisionId}", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotReturnRevisionWhenRevisionIsNotFound() throws Exception {
        mvc.perform(get("/revisions/{revisionId}", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldReturnRevisions() throws Exception {
        DocumentRevision firstRevision = DocumentRevision.builder()
                                                         .author(author)
                                                         .document(document)
                                                         .version(1L)
                                                         .name(firstFile.getOriginalFilename())
                                                         .type(firstFile.getContentType())
                                                         .size(firstFile.getSize())
                                                         .hash(firstHash)
                                                         .build();

        DocumentRevision secondRevision = DocumentRevision.builder()
                                                          .author(author)
                                                          .document(document)
                                                          .version(2L)
                                                          .name(secondFile.getOriginalFilename())
                                                          .type(secondFile.getContentType())
                                                          .size(secondFile.getSize())
                                                          .hash(secondHash)
                                                          .build();

        DocumentRevision thirdRevision = DocumentRevision.builder()
                                                         .author(author)
                                                         .document(document)
                                                         .version(3L)
                                                         .name(thirdFile.getOriginalFilename())
                                                         .type(thirdFile.getContentType())
                                                         .size(thirdFile.getSize())
                                                         .hash(thirdHash)
                                                         .build();

        List<DocumentRevision> revisions = new ArrayList<>();
        revisions.add(firstRevision);
        revisions.add(secondRevision);
        revisions.add(thirdRevision);

        document.setRevisions(revisions);

        userRepository.save(author);
        documentRepository.save(document);

        revisionRepository.save(firstRevision);
        revisionRepository.save(secondRevision);
        revisionRepository.save(thirdRevision);

        mvc.perform(get("/revisions")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("page", "0")
               .param("limit", "3")
               .param("sort", "name:desc,type:asc")
               .param("filter", "name:\"doc\",type:\"text\""))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.content").isArray(),
               jsonPath("$.number").value(0),
               jsonPath("$.totalElements").value(3),
               jsonPath("$.content[0]").isNotEmpty(),
               jsonPath("$.content[0].revisionId").value(secondRevision.getRevisionId()),
               jsonPath("$.content[1]").isNotEmpty(),
               jsonPath("$.content[1].revisionId").value(thirdRevision.getRevisionId()),
               jsonPath("$.content[2]").isNotEmpty(),
               jsonPath("$.content[2].revisionId").value(firstRevision.getRevisionId())
           );
    }

    @ParameterizedTest
    @CsvSource(
        {
            "revision_id, asc",
            "name, asc",
            "type, asc",
            "size, asc",
            "version, asc",
            "created_at, asc"
        }
    )
    void shouldReturnSortedRevisions(String field, String order) throws Exception {
        DocumentRevision firstRevision = DocumentRevision.builder()
                                                         .author(author)
                                                         .document(document)
                                                         .version(1L)
                                                         .name(firstFile.getOriginalFilename())
                                                         .type(firstFile.getContentType())
                                                         .size(firstFile.getSize())
                                                         .hash(firstHash)
                                                         .build();

        List<DocumentRevision> revisions = new ArrayList<>();
        revisions.add(firstRevision);

        document.setRevisions(revisions);

        userRepository.save(author);
        documentRepository.save(document);
        revisionRepository.save(firstRevision);

        mvc.perform(get("/revisions")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("sort", field + ":" + order))
           .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource(
        {
            "name, value",
            "type, value"
        }
    )
    void shouldReturnFilteredRevisions(String field, String value) throws Exception {
        DocumentRevision firstRevision = DocumentRevision.builder()
                                                         .author(author)
                                                         .document(document)
                                                         .version(1L)
                                                         .name(firstFile.getOriginalFilename())
                                                         .type(firstFile.getContentType())
                                                         .size(firstFile.getSize())
                                                         .hash(firstHash)
                                                         .build();

        List<DocumentRevision> revisions = new ArrayList<>();
        revisions.add(firstRevision);

        document.setRevisions(revisions);

        userRepository.save(author);
        documentRepository.save(document);
        revisionRepository.save(firstRevision);

        mvc.perform(get("/revisions")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("filter", field + ":\"" + value + "\""))
           .andExpect(status().isOk());
    }

    @Test
    void shouldNotReturnRevisionsWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(get("/revisions"))
           .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"name=desc", "name:name", "name::desc", "name:asc|type:desc"})
    void shouldNotReturnRevisionsWhenSortHasInvalidFormat(String sort) throws Exception {
        DocumentRevision firstRevision = DocumentRevision.builder()
                                                         .author(author)
                                                         .document(document)
                                                         .version(1L)
                                                         .name(firstFile.getOriginalFilename())
                                                         .type(firstFile.getContentType())
                                                         .size(firstFile.getSize())
                                                         .hash(firstHash)
                                                         .build();

        List<DocumentRevision> revisions = new ArrayList<>();
        revisions.add(firstRevision);

        document.setRevisions(revisions);

        userRepository.save(author);
        documentRepository.save(document);
        revisionRepository.save(firstRevision);

        mvc.perform(get("/revisions")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("sort", sort))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("sort")),
               jsonPath("$.detail").value(containsString("does not match"))
           );
    }

    @ParameterizedTest
    @ValueSource(strings = {"name:doc", "name:\"doc", "name=doc", "name:\"doc\"|type:\"text\""})
    void shouldNotReturnRevisionsWhenFilterHasInvalidFormat(String filter) throws Exception {
        DocumentRevision firstRevision = DocumentRevision.builder()
                                                         .author(author)
                                                         .document(document)
                                                         .version(1L)
                                                         .name(firstFile.getOriginalFilename())
                                                         .type(firstFile.getContentType())
                                                         .size(firstFile.getSize())
                                                         .hash(firstHash)
                                                         .build();

        List<DocumentRevision> revisions = new ArrayList<>();
        revisions.add(firstRevision);

        document.setRevisions(revisions);

        userRepository.save(author);
        documentRepository.save(document);
        revisionRepository.save(firstRevision);

        mvc.perform(get("/revisions")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("filter", filter))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("filter")),
               jsonPath("$.detail").value(containsString("does not match"))
           );
    }

}