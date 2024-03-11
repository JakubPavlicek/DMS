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
import org.springframework.http.HttpMethod;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private User author;
    private Document document;
    private Document secondDocument;
    private DocumentRevision firstRevision;
    private DocumentRevision secondRevision;
    private DocumentRevision thirdRevision;

    private final SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + Role.USER.name());

    @BeforeEach
    void setUp() throws IOException {
        DirectoryCleaner.cleanDirectory(blobStorageProperties.getPath());

        firstFile = new MockMultipartFile("first_file", "document.txt", MediaType.TEXT_PLAIN_VALUE, "first".getBytes());
        firstHash = blobStorageService.storeBlob(firstFile);

        secondFile = new MockMultipartFile("second_file", "temp_document.txt", MediaType.TEXT_PLAIN_VALUE, "second".getBytes());
        String secondHash = blobStorageService.storeBlob(secondFile);

        thirdFile = new MockMultipartFile("third_file", "final_document.txt", MediaType.TEXT_PLAIN_VALUE, "third".getBytes());
        String thirdHash = blobStorageService.storeBlob(thirdFile);

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

        secondDocument = Document.builder()
                                 .author(author)
                                 .version(1L)
                                 .name(secondFile.getOriginalFilename())
                                 .type(secondFile.getContentType())
                                 .path("/home")
                                 .size(secondFile.getSize())
                                 .hash(secondHash)
                                 .isArchived(false)
                                 .build();

        firstRevision = DocumentRevision.builder()
                                        .author(author)
                                        .document(document)
                                        .version(1L)
                                        .name(firstFile.getOriginalFilename())
                                        .type(firstFile.getContentType())
                                        .size(firstFile.getSize())
                                        .hash(firstHash)
                                        .build();

        secondRevision = DocumentRevision.builder()
                                         .author(author)
                                         .document(document)
                                         .version(2L)
                                         .name(secondFile.getOriginalFilename())
                                         .type(secondFile.getContentType())
                                         .size(secondFile.getSize())
                                         .hash(secondHash)
                                         .build();

        thirdRevision = DocumentRevision.builder()
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
        documentRepository.save(secondDocument);

        revisionRepository.save(firstRevision);
        revisionRepository.save(secondRevision);
        revisionRepository.save(thirdRevision);
    }

    @AfterEach
    void tearDown() throws Exception {
        DirectoryCleaner.cleanDirectory(blobStorageProperties.getPath());
    }

    @Test
    void shouldArchiveDocument() throws Exception {
        mvc.perform(put("/documents/{documentId}/archive", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpect(status().isNoContent());

        Optional<Document> archivedDocument = documentRepository.findById(document.getId());

        assertThat(archivedDocument).isPresent();
        assertThat(archivedDocument.get().getIsArchived()).isTrue();
        assertThat(archivedDocument.get().getDeleteAt()).isNotNull();
    }

    @Test
    void shouldNotArchiveDocumentWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(put("/documents/{documentId}/archive", document.getDocumentId()))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotArchiveDocumentWhenDocumentIsNotFound() throws Exception {
        mvc.perform(put("/documents/{documentId}/archive", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldDeleteDocumentWithRevisions() throws Exception {
        mvc.perform(delete("/documents/{documentId}", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpect(status().isNoContent());

        Optional<Document> documentById = documentRepository.findById(document.getId());
        Optional<DocumentRevision> currentRevisionById = revisionRepository.findById(firstRevision.getId());
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
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldDownloadDocument() throws Exception {
        mvc.perform(get("/documents/{documentId}/download", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
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
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldNotDownloadDocumentWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(get("/documents/{documentId}/download", document.getDocumentId()))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotDownloadDocumentWhenBlobDoesNotExists() throws Exception {
        blobStorageService.deleteBlob(firstHash);

        mvc.perform(get("/documents/{documentId}/download", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isInternalServerError(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value("File could not be resolved")
           );
    }

    @Test
    void shouldReturnDocument() throws Exception {
        mvc.perform(get("/documents/{documentId}", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.documentId").value(document.getDocumentId()),
               jsonPath("$.author.userId").value(author.getUserId()),
               jsonPath("$.version").value(document.getVersion()),
               jsonPath("$.name").value(document.getName()),
               jsonPath("$.type").value(document.getType()),
               jsonPath("$.path").value(document.getPath()),
               jsonPath("$.size").value(document.getSize()),
               jsonPath("$.createdAt").isNotEmpty(),
               jsonPath("$.updatedAt").isNotEmpty()
           );
    }

    @Test
    void shouldNotReturnDocumentWhenDocumentIsNotFound() throws Exception {
        mvc.perform(get("/documents/{documentId}", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldNotReturnDocumentWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(get("/documents/{documentId}", document.getDocumentId()))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnDocumentRevisions() throws Exception {
        mvc.perform(get("/documents/{documentId}/revisions", document.getDocumentId())
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
    void shouldReturnSortedDocumentRevisions(String field, String order) throws Exception {
        mvc.perform(get("/documents/{documentId}/revisions", document.getDocumentId())
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
    void shouldReturnFilteredDocumentRevisions(String field, String value) throws Exception {
        mvc.perform(get("/documents/{documentId}/revisions", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("filter", field + ":\"" + value + "\""))
           .andExpect(status().isOk());
    }

    @Test
    void shouldNotReturnDocumentRevisionsWhenDocumentIsNotFound() throws Exception {
        mvc.perform(get("/documents/{documentId}/revisions", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldNotReturnDocumentRevisionsWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(get("/documents/{documentId}/revisions", document.getDocumentId()))
           .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"name=desc", "name:name", "name::desc", "name:asc|type:desc"})
    void shouldNotReturnDocumentRevisionsWhenSortHasInvalidFormat(String sort) throws Exception {
        mvc.perform(get("/documents/{documentId}/revisions", document.getDocumentId())
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
    void shouldNotReturnDocumentRevisionsWhenFilterHasInvalidFormat(String filter) throws Exception {
        mvc.perform(get("/documents/{documentId}/revisions", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("filter", filter))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("filter")),
               jsonPath("$.detail").value(containsString("does not match"))
           );
    }

    @Test
    void shouldReturnDocuments() throws Exception {
        mvc.perform(get("/documents")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("page", "0")
               .param("limit", "2")
               .param("sort", "name:desc,type:asc")
               .param("filter", "name:\"doc\",type:\"text\",is_archived:\"false\""))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.content").isArray(),
               jsonPath("$.number").value(0),
               jsonPath("$.totalElements").value(2),
               jsonPath("$.content[0]").isNotEmpty(),
               jsonPath("$.content[0].documentId").value(secondDocument.getDocumentId()),
               jsonPath("$.content[1]").isNotEmpty(),
               jsonPath("$.content[1].documentId").value(document.getDocumentId())
           );
    }

    @ParameterizedTest
    @CsvSource(
        {
            "document_id, asc",
            "name, asc",
            "type, asc",
            "path, asc",
            "size, asc",
            "version, asc",
            "created_at, asc",
            "updated_at, asc"
        }
    )
    void shouldReturnSortedDocuments(String field, String order) throws Exception {
        mvc.perform(get("/documents")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("sort", field + ":" + order))
           .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource(
        {
            "name, value",
            "type, value",
            "path, value",
            "is_archived, value"
        }
    )
    void shouldReturnFilteredDocuments(String field, String value) throws Exception {
        mvc.perform(get("/documents")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("filter", field + ":\"" + value + "\""))
           .andExpect(status().isOk());
    }

    @Test
    void shouldNotReturnDocumentsWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(get("/documents"))
           .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"name=desc", "name:name", "name::desc", "name:asc|type:desc"})
    void shouldNotReturnDocumentsWhenSortHasInvalidFormat(String sort) throws Exception {
        mvc.perform(get("/documents")
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
    void shouldNotReturnDocumentsWhenFilterHasInvalidFormat(String filter) throws Exception {
        mvc.perform(get("/documents")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .param("filter", filter))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("filter")),
               jsonPath("$.detail").value(containsString("does not match"))
           );
    }

    @Test
    void shouldMoveDocument() throws Exception {
        mvc.perform(put("/documents/{documentId}/move", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"path": "/home"
                        }
                        """))
           .andExpectAll(
               status().isOk(),
               jsonPath("$.documentId").value(document.getDocumentId()),
               jsonPath("$.path").value("/home")
           );
    }

    @Test
    void shouldNotMoveDocumentWhenPathIsNull() throws Exception {
        mvc.perform(put("/documents/{documentId}/move", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.APPLICATION_JSON)
               .content("{}"))
           .andExpectAll(
               status().isBadRequest(),
               jsonPath("$.context_info.messages[0]").value("path: must not be null")
           );
    }

    @Test
    void shouldNotMoveDocumentWhenPathIsInvalid() throws Exception {
        mvc.perform(put("/documents/{documentId}/move", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"path": "/home!!!"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               jsonPath("$.context_info.messages[0]").value(containsString("path: must match"))
           );
    }

    @Test
    void shouldNotMoveDocumentWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(put("/documents/{documentId}/move", document.getDocumentId()))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotMoveDocumentWhenPathAlreadyExists() throws Exception {
        mvc.perform(put("/documents/{documentId}/move", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"path": "/"
                        }
                        """))
           .andExpectAll(
               status().isConflict(),
               jsonPath("$.detail").value(containsString("path")),
               jsonPath("$.detail").value(containsString("already exists"))
           );
    }

    @Test
    void shouldNotMoveDocumentWhenNoDataAreProvided() throws Exception {
        mvc.perform(put("/documents/{documentId}/move", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isUnsupportedMediaType(),
               jsonPath("$.context_info.messages[0]").value("Request must contain data")
           );
    }

    @Test
    void shouldRestoreDocument() throws Exception {
        mvc.perform(put("/documents/{documentId}/restore", document.getDocumentId())
            .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
            .andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.documentId").value(document.getDocumentId()),
                jsonPath("$.isArchived").value("false")
            );

        Optional<Document> restoredDocument = documentRepository.findById(document.getId());

        assertThat(restoredDocument).isPresent();
        assertThat(restoredDocument.get().getDeleteAt()).isNull();
    }

    @Test
    void shouldNotRestoreDocumentWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(put("/documents/{documentId}/restore", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotRestoreDocumentWhenDocumentIsNotFound() throws Exception {
        mvc.perform(put("/documents/{documentId}/restore", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldSwitchDocumentToRevision() throws Exception {
        mvc.perform(put("/documents/{documentId}/revisions/{revisionId}", document.getDocumentId(), secondRevision.getRevisionId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.documentId").value(document.getDocumentId()),
               jsonPath("$.author.userId").value(author.getUserId()),
               jsonPath("$.version").value(secondRevision.getVersion()),
               jsonPath("$.name").value(secondRevision.getName()),
               jsonPath("$.type").value(secondRevision.getType()),
               jsonPath("$.path").value(document.getPath()),
               jsonPath("$.size").value(document.getSize())
           );

        Optional<Document> documentById = documentRepository.findById(document.getId());

        assertThat(documentById).isPresent();
        assertThat(documentById.get()
                               .getHash()).isEqualTo(secondRevision.getHash());
    }

    @Test
    void shouldNotSwitchDocumentToRevisionWhenUserIsUnauthenticated() throws Exception {
        mvc.perform(put("/documents/{documentId}/revisions/{revisionId}", document.getDocumentId(), secondRevision.getRevisionId()))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotSwitchDocumentToRevisionWhenDocumentIsNotFound() throws Exception {
        mvc.perform(put("/documents/{documentId}/revisions/{revisionId}", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed", secondRevision.getRevisionId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldNotSwitchDocumentToRevisionWhenRevisionIsNotFound() throws Exception {
        mvc.perform(put("/documents/{documentId}/revisions/{revisionId}", document.getDocumentId(), "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldUploadDocument() throws Exception {
        revisionRepository.deleteAll();
        documentRepository.deleteAll();

        MockMultipartFile file = new MockMultipartFile("file", firstFile.getOriginalFilename(), firstFile.getContentType(), firstFile.getBytes());
        MockMultipartFile destination = new MockMultipartFile("destination", "", MediaType.APPLICATION_JSON_VALUE, "{\"path\":\"/home\"}".getBytes());

        mvc.perform(multipart(HttpMethod.POST, "/documents/upload")
               .file(file)
               .file(destination)
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isCreated(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.documentId").isNotEmpty(),
               jsonPath("$.author.userId").value(author.getUserId()),
               jsonPath("$.version").value(1L),
               jsonPath("$.name").value(firstFile.getOriginalFilename()),
               jsonPath("$.type").value(MediaType.TEXT_PLAIN_VALUE),
               jsonPath("$.path").value("/home"),
               jsonPath("$.size").value(firstFile.getSize())
           );

        List<Document> documents = documentRepository.findAll();

        assertThat(documents).hasSize(1);
    }

    @Test
    void shouldNotUploadDocumentWhenFileIsMissing() throws Exception {
        MockMultipartFile destination = new MockMultipartFile("destination", "", MediaType.APPLICATION_JSON_VALUE, "{\"path\":\"/home\"}".getBytes());

        mvc.perform(multipart(HttpMethod.POST, "/documents/upload")
               .file(destination)
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("file")),
               jsonPath("$.detail").value(containsString("is not present"))
           );
    }

    @Test
    void shouldNotUploadDocumentWhenDestinationIsMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", secondFile.getOriginalFilename(), secondFile.getContentType(), secondFile.getBytes());

        mvc.perform(multipart(HttpMethod.POST, "/documents/upload")
               .file(file)
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("destination")),
               jsonPath("$.detail").value(containsString("is not present"))
           );
    }

    @Test
    void shouldNotUploadDocumentWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(multipart(HttpMethod.POST, "/documents/upload"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotUploadDocumentWhenDocumentWithPathAlreadyExists() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", firstFile.getOriginalFilename(), firstFile.getContentType(), firstFile.getBytes());
        MockMultipartFile destination = new MockMultipartFile("destination", "", MediaType.APPLICATION_JSON_VALUE, "{\"path\":\"/\"}".getBytes());

        mvc.perform(multipart(HttpMethod.POST, "/documents/upload")
               .file(file)
               .file(destination)
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isConflict(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("path")),
               jsonPath("$.detail").value(containsString("already exists"))
           );
    }

    @Test
    void shouldUploadNewDocumentVersion() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", thirdFile.getOriginalFilename(), thirdFile.getContentType(), thirdFile.getBytes());
        MockMultipartFile destination = new MockMultipartFile("destination", "", MediaType.APPLICATION_JSON_VALUE, "{\"path\":\"/test\"}".getBytes());

        mvc.perform(multipart(HttpMethod.PUT, "/documents/{documentId}", document.getDocumentId())
               .file(file)
               .file(destination)
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isCreated(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.documentId").value(document.getDocumentId()),
               jsonPath("$.author.userId").value(author.getUserId()),
               jsonPath("$.version").value(4L),
               jsonPath("$.name").value(thirdFile.getOriginalFilename()),
               jsonPath("$.type").value(MediaType.TEXT_PLAIN_VALUE),
               jsonPath("$.path").value("/test"),
               jsonPath("$.size").value(thirdFile.getSize())
           );
    }

    @Test
    void shouldUploadNewDocumentVersionWithNullDestination() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", thirdFile.getOriginalFilename(), thirdFile.getContentType(), thirdFile.getBytes());

        mvc.perform(multipart(HttpMethod.PUT, "/documents/{documentId}", document.getDocumentId())
               .file(file)
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isCreated(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.documentId").value(document.getDocumentId()),
               jsonPath("$.author.userId").value(author.getUserId()),
               jsonPath("$.version").value(4L),
               jsonPath("$.name").value(thirdFile.getOriginalFilename()),
               jsonPath("$.type").value(MediaType.TEXT_PLAIN_VALUE),
               jsonPath("$.path").value(document.getPath()),
               jsonPath("$.size").value(thirdFile.getSize())
           );
    }

    @Test
    void shouldNotUploadNewDocumentVersionWhenFileIsMissing() throws Exception {
        mvc.perform(multipart(HttpMethod.PUT, "/documents/{documentId}", document.getDocumentId())
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("file")),
               jsonPath("$.detail").value(containsString("is not present"))
           );
    }

    @Test
    void shouldNotUploadNewDocumentVersionWhenDestinationIsInvalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", thirdFile.getOriginalFilename(), thirdFile.getContentType(), thirdFile.getBytes());
        MockMultipartFile destination = new MockMultipartFile("destination", "", MediaType.APPLICATION_JSON_VALUE, "{\"path\":\"/!!!\"}".getBytes());

        mvc.perform(multipart(HttpMethod.PUT, "/documents/{documentId}", document.getDocumentId())
               .file(file)
               .file(destination)
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value("Provided data are not valid")
           );
    }

    @Test
    void shouldNotUploadNewDocumentVersionWhenUserIsNotAuthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", thirdFile.getOriginalFilename(), thirdFile.getContentType(), thirdFile.getBytes());
        MockMultipartFile destination = new MockMultipartFile("destination", "", MediaType.APPLICATION_JSON_VALUE, "{\"path\":\"/home\"}".getBytes());

        mvc.perform(multipart(HttpMethod.PUT, "/documents/{documentId}", document.getDocumentId())
               .file(file)
               .file(destination)
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotUploadNewDocumentVersionWhenDocumentIsNotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", thirdFile.getOriginalFilename(), thirdFile.getContentType(), thirdFile.getBytes());
        MockMultipartFile destination = new MockMultipartFile("destination", "", MediaType.APPLICATION_JSON_VALUE, "{\"path\":\"/home\"}".getBytes());

        mvc.perform(multipart(HttpMethod.PUT, "/documents/{documentId}", "65be38e5-a749-4dc7-b6d4-8ca2c150aaed")
               .file(file)
               .file(destination)
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isNotFound(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldNotUploadNewDocumentVersionWhenDocumentWithPathAlreadyExists() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", firstFile.getOriginalFilename(), firstFile.getContentType(), firstFile.getBytes());
        MockMultipartFile destination = new MockMultipartFile("destination", "", MediaType.APPLICATION_JSON_VALUE, "{\"path\":\"/\"}".getBytes());

        mvc.perform(multipart(HttpMethod.PUT, "/documents/{documentId}", document.getDocumentId())
               .file(file)
               .file(destination)
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isConflict(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("path")),
               jsonPath("$.detail").value(containsString("already exists"))
           );
    }

    @Test
    void shouldNotUploadNewDocumentVersionWhenFileIsNull() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", (byte[]) null);
        MockMultipartFile destination = new MockMultipartFile("destination", "", MediaType.APPLICATION_JSON_VALUE, "{\"path\":\"/\"}".getBytes());

        mvc.perform(multipart(HttpMethod.PUT, "/documents/{documentId}", document.getDocumentId())
               .file(file)
               .file(destination)
               .with(jwt().authorities(authority).jwt(JwtManager.createJwt(author.getEmail())))
               .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value(containsString("file"))
           );
    }

}