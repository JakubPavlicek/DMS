package com.dms.integration.repository;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import com.dms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DocumentRevisionRepositoryTest {

    @Autowired
    private DocumentRevisionRepository revisionRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    private DocumentRevision revision;

    @BeforeEach
    void setUp() {
        User author = createUser();
        author = userRepository.save(author);

        Document document = createDocument(author);
        document = documentRepository.save(document);

        revision = createRevision(author, document);
        revision = revisionRepository.save(revision);
    }

    @Test
    void whenValidRevisionIdAndAuthor_thenRevisionShouldBeFound() {
        assertThat(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), revision.getAuthor())).isPresent();
    }

    @Test
    void whenInvalidRevisionIdAndValidAuthor_thenNoRevisionShouldBeFound() {
        String revisionId = "09bb6fd7-8c8f-4508-9b7b-417f0fe501ba";
        assertThat(revisionRepository.findByRevisionIdAndAuthor(revisionId, revision.getAuthor())).isEmpty();
    }

    @Test
    void whenInvalidAuthorAndValidRevisionId_thenNoRevisionShouldBeFound() {
        User author = createUser();
        author.setUserId("8d118708-9e09-4053-883d-8c2079f5c0a3");
        author.setEmail("john@email.com");
        author = userRepository.save(author);

        assertThat(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), author)).isEmpty();
    }

    @Test
    void whenValidDocumentAndRevisionId_thenRevisionShouldBeFound() {
        assertThat(revisionRepository.findByDocumentAndRevisionId(revision.getDocument(), revision.getRevisionId())).isPresent();
    }

    @Test
    void whenInvalidRevisionIdAndValidDocument_thenNoRevisionShouldBeFound() {
        String revisionId = "6cf458dc-ee8c-47cb-accc-4a4f5ece0c8f";
        assertThat(revisionRepository.findByDocumentAndRevisionId(revision.getDocument(), revisionId)).isEmpty();
    }

    @Test
    void whenInvalidDocumentAndValidRevisionId_thenNoRevisionShouldBeFound() {
        User author = createUser();
        author.setUserId("ad9f1826-f847-4069-b57c-938b8d843c1d");
        author.setEmail("john@gmail.com");
        author = userRepository.save(author);

        Document document = createDocument(author);
        document.setDocumentId("57c00a2f-8435-4ea4-91d9-e195680dea37");
        document = documentRepository.save(document);

        assertThat(revisionRepository.findByDocumentAndRevisionId(document, revision.getRevisionId())).isEmpty();
    }

    @Test
    void whenTwoRevisionsExist_thenRevisionCountShouldBeTwo() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setRevisionId("57c00a2f-8435-4ea4-91d9-e195680dea37");
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.findAllByDocumentOrderByCreatedAtAsc(revision.getDocument()).size()).isEqualTo(2);
    }

    @Test
    void whenTwoReivisonsExist_thenTwoRevisionsOrderedByCreatedAtAscShouldBeReturned() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setRevisionId("57c00a2f-8435-4ea4-91d9-e195680dea37");
        anotherRevision.setCreatedAt(LocalDateTime.parse("2023-11-14T08:30:00").minusHours(1));
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.findAllByDocumentOrderByCreatedAtAsc(revision.getDocument())).extracting(DocumentRevision::getCreatedAt).isSortedAccordingTo(Comparator.naturalOrder());
    }

    @Test
    void whenValidRevisionId_thenRevisionShouldBeDeleted() {
        revisionRepository.deleteByRevisionId(revision.getRevisionId());
        assertThat(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), revision.getAuthor())).isEmpty();
    }

    @Test
    void whenTwoDuplicateHashesExist_thenShouldReturnTrue() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setRevisionId("57c00a2f-8435-4ea4-91d9-e195680dea37");
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.duplicateHashExists(revision.getHash())).isTrue();
    }

    @Test
    void whenTwoDistinctHashesExist_thenShouldReturnFalse() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setRevisionId("57c00a2f-8435-4ea4-91d9-e195680dea37");
        anotherRevision.setHash("04cda4ddf5773cbc4f80452696112091f60380015b17973aae8a11f3d92e7c7d");
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.duplicateHashExists(revision.getHash())).isFalse();
    }

    @Test
    void whenValidDocumentAndVersion_thenPreviousRevisionShouldBeFound() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setRevisionId("8d118708-9e09-4053-883d-8c2079f5c0a3");
        anotherRevision.setVersion(revision.getVersion() + 1);
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.findPreviousByDocumentAndVersion(revision.getDocument(), anotherRevision.getVersion())).isPresent();
    }

    @Test
    void whenInvalidDocumentAndValidVersion_thenNoPreviousRevisionShouldBeFound() {
        Document document = createDocument(revision.getAuthor());
        document.setDocumentId("8d118708-9e09-4053-883d-8c2079f5c0a3");
        document = documentRepository.save(document);

        assertThat(revisionRepository.findPreviousByDocumentAndVersion(document, revision.getVersion())).isEmpty();
    }

    @Test
    void whenRevisionIsTheOnlyOne_thenNoPreviousRevisionShouldBeFound() {
        assertThat(revisionRepository.findPreviousByDocumentAndVersion(revision.getDocument(), revision.getVersion())).isEmpty();
    }

    @Test
    void whenValidDocumentAndVersion_thenNextRevisionShouldBeFound() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setRevisionId("8d118708-9e09-4053-883d-8c2079f5c0a3");
        anotherRevision.setVersion(revision.getVersion() + 1);
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.findNextByDocumentAndVersion(revision.getDocument(), revision.getVersion())).isPresent();
    }

    @Test
    void whenInvalidDocumentAndValidVersion_thenNoNextRevisionShouldBeFound() {
        Document document = createDocument(revision.getAuthor());
        document.setDocumentId("8d118708-9e09-4053-883d-8c2079f5c0a3");
        document = documentRepository.save(document);

        assertThat(revisionRepository.findNextByDocumentAndVersion(document, revision.getVersion())).isEmpty();
    }

    @Test
    void whenRevisionIsTheOnlyOne_thenNoNextRevisionShouldBeFound() {
        assertThat(revisionRepository.findNextByDocumentAndVersion(revision.getDocument(), revision.getVersion())).isEmpty();
    }

    @Test
    void whenRevisionWithVersionOneIsTheOnlyOne_thenLastRevisionVersionShouldBeOne() {
        assertThat(revisionRepository.findLastRevisionVersionByDocument(revision.getDocument()).get()).isEqualTo(1);
    }

    @Test
    void whenThreeRevisionsExist_thenLastRevisionVersionShouldBeThree() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setRevisionId("57c00a2f-8435-4ea4-91d9-e195680dea37");
        anotherRevision.setVersion(revision.getVersion() + 1);
        revisionRepository.save(anotherRevision);

        DocumentRevision anotherRevision2 = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision2.setRevisionId("8d118708-9e09-4053-883d-8c2079f5c0a3");
        anotherRevision2.setVersion(anotherRevision.getVersion() + 1);
        revisionRepository.save(anotherRevision2);

        assertThat(revisionRepository.findLastRevisionVersionByDocument(revision.getDocument()).get()).isEqualTo(3);
    }

    @Test
    void whenInvalidDocument_thenNoRevisionVersionShouldBeFound() {
        Document document = createDocument(revision.getAuthor());
        document.setDocumentId("8d118708-9e09-4053-883d-8c2079f5c0a3");
        document = documentRepository.save(document);

        assertThat(revisionRepository.findLastRevisionVersionByDocument(document)).isEmpty();
    }

    private DocumentRevision createRevision(User author, Document document) {
        return DocumentRevision.builder()
                               .revisionId("95f6dbc2-b919-4b04-94b6-e857a92677d4")
                               .author(author)
                               .document(document)
                               .version(1L)
                               .name("dog.jpeg")
                               .type("image/jpeg")
                               .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                               .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                               .build();
    }

    private Document createDocument(User author) {
        return Document.builder()
                       .author(author)
                       .documentId("277f6b39-ec44-4fbe-9605-8d0dee790518")
                       .version(1L)
                       .name("dog.jpeg")
                       .type("image/jpeg")
                       .path("/test")
                       .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                       .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                       .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                       .build();
    }

    private User createUser() {
        return User.builder()
                   .userId("fde1be20-54b1-41f6-8506-bdd0d63c189f")
                   .name("james")
                   .email("james@gmail.com")
                   .password("secret123!")
                   .build();
    }

}