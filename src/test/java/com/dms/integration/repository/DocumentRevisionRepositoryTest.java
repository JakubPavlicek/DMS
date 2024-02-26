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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
        User author = User.builder()
                          .userId("fde1be20-54b1-41f6-8506-bdd0d63c189f")
                          .name("james")
                          .email("james@gmail.com")
                          .password("secret123!")
                          .build();

        author = userRepository.save(author);

        Document document = Document.builder()
                                    .author(author)
                                    .documentId("277f6b39-ec44-4fbe-9605-8d0dee790518")
                                    .version(1L)
                                    .name("dog.jpeg")
                                    .type("image/jpeg")
                                    .path("/test")
                                    .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                    .isArchived(false)
                                    .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .build();

        document = documentRepository.save(document);

        revision = DocumentRevision.builder()
                                   .revisionId("95f6dbc2-b919-4b04-94b6-e857a92677d4")
                                   .author(author)
                                   .document(document)
                                   .version(1L)
                                   .name("dog.jpeg")
                                   .type("image/jpeg")
                                   .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                   .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                                   .build();

        revision = revisionRepository.save(revision);
    }

    @Test
    void shouldFindRevisionByRevisionIdAndAuthor() {
        Optional<DocumentRevision> foundRevision = revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), revision.getAuthor());

        assertThat(foundRevision).isPresent();
    }

    @Test
    void shouldNotFindRevisionByRevisionIdAndAuthorWhenRevisionIdIsInvalid() {
        Optional<DocumentRevision> foundRevision = revisionRepository.findByRevisionIdAndAuthor("09bb6fd7-8c8f-4508-9b7b-417f0fe501ba", revision.getAuthor());

        assertThat(foundRevision).isEmpty();
    }

    @Test
    void shouldNotFindRevisionWhenAuthorIsInvalid() {
        User author = User.builder()
                          .userId("8d118708-9e09-4053-883d-8c2079f5c0a3")
                          .name("john")
                          .email("john@email.com")
                          .password("secret123!")
                          .build();

        author = userRepository.save(author);

        Optional<DocumentRevision> foundRevision = revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), author);

        assertThat(foundRevision).isEmpty();
    }

    @Test
    void shouldFindRevisionWhenByDocumentAndRevisionId() {
        Optional<DocumentRevision> foundRevision = revisionRepository.findByDocumentAndRevisionId(revision.getDocument(), revision.getRevisionId());

        assertThat(foundRevision).isPresent();
    }

    @Test
    void shouldNotFindRevisionByDocumentAndRevisionIdWhenRevisionIdIsInvalid() {
        Optional<DocumentRevision> foundRevision = revisionRepository.findByDocumentAndRevisionId(revision.getDocument(), "6cf458dc-ee8c-47cb-accc-4a4f5ece0c8f");

        assertThat(foundRevision).isEmpty();
    }

    @Test
    void shouldNotFindRevisionByDocumentAndRevisionIdWhenDocumentIsInvalid() {
        User author = User.builder()
                          .userId("8d118708-9e09-4053-883d-8c2079f5c0a3")
                          .name("john")
                          .email("john@email.com")
                          .password("secret123!")
                          .build();

        author = userRepository.save(author);

        Document document = Document.builder()
                                    .author(author)
                                    .documentId("57c00a2f-8435-4ea4-91d9-e195680dea37")
                                    .version(1L)
                                    .name("dog.jpeg")
                                    .type("image/jpeg")
                                    .path("/test")
                                    .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                    .isArchived(false)
                                    .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .build();

        document = documentRepository.save(document);

        Optional<DocumentRevision> foundRevision = revisionRepository.findByDocumentAndRevisionId(document, this.revision.getRevisionId());

        assertThat(foundRevision).isEmpty();
    }

    @Test
    void shouldFindTwoRevisions() {
        DocumentRevision anotherRevision = DocumentRevision.builder()
                                                           .revisionId("57c00a2f-8435-4ea4-91d9-e195680dea37")
                                                           .author(revision.getAuthor())
                                                           .document(revision.getDocument())
                                                           .version(1L)
                                                           .name("dog.jpeg")
                                                           .type("image/jpeg")
                                                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                                                           .build();

        revisionRepository.save(anotherRevision);

        int revisionCount = revisionRepository.findAllByDocumentOrderByCreatedAtAsc(revision.getDocument())
                                              .size();

        assertThat(revisionCount).isEqualTo(2);
    }

    @Test
    void shouldReturnTwoRevisionsOrderedByCreatedAtAsc() {
        DocumentRevision anotherRevision = DocumentRevision.builder()
                                                           .revisionId("57c00a2f-8435-4ea4-91d9-e195680dea37")
                                                           .author(revision.getAuthor())
                                                           .document(revision.getDocument())
                                                           .version(1L)
                                                           .name("dog.jpeg")
                                                           .type("image/jpeg")
                                                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:00")
                                                                                   .minusHours(1))
                                                           .build();

        revisionRepository.save(anotherRevision);

        List<DocumentRevision> revisions = revisionRepository.findAllByDocumentOrderByCreatedAtAsc(revision.getDocument());

        assertThat(revisions).extracting(DocumentRevision::getCreatedAt)
                             .isSortedAccordingTo(Comparator.naturalOrder());
    }

    @Test
    void shouldDeleteRevision() {
        revisionRepository.deleteByRevisionId(revision.getRevisionId());

        Optional<DocumentRevision> foundRevision = revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), revision.getAuthor());

        assertThat(foundRevision).isEmpty();
    }

    @Test
    void shouldNotDeleteRevision() {
        revisionRepository.deleteByRevisionId("57c00a2f-8435-4ea4-91d9-e195680dea37");

        Optional<DocumentRevision> foundRevision = revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), revision.getAuthor());

        assertThat(foundRevision).isPresent();
    }

    @Test
    void shouldReturnRevisionCountForDocument() {
        Integer revisionCount = revisionRepository.countAllByDocument(revision.getDocument());

        assertThat(revisionCount).isEqualTo(1);
    }

    @Test
    void shouldReturnPagedRevisionsForDocument() {
        Page<DocumentRevision> revisions = revisionRepository.findAllByDocument(revision.getDocument(), PageRequest.of(0, 2));

        assertThat(revisions).isNotEmpty();
        assertThat(revisions.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnTrueWhenTwoDuplicateHashesExist() {
        DocumentRevision anotherRevision = DocumentRevision.builder()
                                                           .revisionId("57c00a2f-8435-4ea4-91d9-e195680dea37")
                                                           .author(revision.getAuthor())
                                                           .document(revision.getDocument())
                                                           .version(1L)
                                                           .name("dog.jpeg")
                                                           .type("image/jpeg")
                                                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                                                           .build();

        revisionRepository.save(anotherRevision);

        boolean duplicateHashExists = revisionRepository.duplicateHashExists(revision.getHash());

        assertThat(duplicateHashExists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTwoDistinctHashesExist() {
        DocumentRevision anotherRevision = DocumentRevision.builder()
                                                           .revisionId("57c00a2f-8435-4ea4-91d9-e195680dea37")
                                                           .author(revision.getAuthor())
                                                           .document(revision.getDocument())
                                                           .version(1L)
                                                           .name("dog.jpeg")
                                                           .type("image/jpeg")
                                                           .hash("04cda4ddf5773cbc4f80452696112091f60380015b17973aae8a11f3d92e7c7d")
                                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                                                           .build();

        revisionRepository.save(anotherRevision);

        boolean duplicateHashExists = revisionRepository.duplicateHashExists(revision.getHash());

        assertThat(duplicateHashExists).isFalse();
    }

    @Test
    void shouldFindPreviousRevision() {
        DocumentRevision anotherRevision = DocumentRevision.builder()
                                                           .revisionId("57c00a2f-8435-4ea4-91d9-e195680dea37")
                                                           .author(revision.getAuthor())
                                                           .document(revision.getDocument())
                                                           .version(2L)
                                                           .name("dog.jpeg")
                                                           .type("image/jpeg")
                                                           .hash("04cda4ddf5773cbc4f80452696112091f60380015b17973aae8a11f3d92e7c7d")
                                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                                                           .build();

        revisionRepository.save(anotherRevision);

        Optional<DocumentRevision> previousRevision = revisionRepository.findPreviousByDocumentAndVersion(revision.getDocument(), anotherRevision.getVersion());

        assertThat(previousRevision).isPresent();
    }

    @Test
    void shouldNotFindPreviousRevisionWhenDocumentIsInvalid() {
        Document document = Document.builder()
                                    .author(revision.getAuthor())
                                    .documentId("8d118708-9e09-4053-883d-8c2079f5c0a3")
                                    .version(1L)
                                    .name("dog.jpeg")
                                    .type("image/jpeg")
                                    .path("/test")
                                    .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                    .isArchived(false)
                                    .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .build();

        document = documentRepository.save(document);

        Optional<DocumentRevision> previousRevision = revisionRepository.findPreviousByDocumentAndVersion(document, revision.getVersion());

        assertThat(previousRevision).isEmpty();
    }

    @Test
    void shouldNotFindPreviousRevisionWhenRevisionIsTheOnlyOne() {
        Optional<DocumentRevision> previousRevision = revisionRepository.findPreviousByDocumentAndVersion(revision.getDocument(), revision.getVersion());

        assertThat(previousRevision).isEmpty();
    }

    @Test
    void shouldFindNextRevision() {
        DocumentRevision anotherRevision = DocumentRevision.builder()
                                                           .revisionId("57c00a2f-8435-4ea4-91d9-e195680dea37")
                                                           .author(revision.getAuthor())
                                                           .document(revision.getDocument())
                                                           .version(2L)
                                                           .name("dog.jpeg")
                                                           .type("image/jpeg")
                                                           .hash("04cda4ddf5773cbc4f80452696112091f60380015b17973aae8a11f3d92e7c7d")
                                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                                                           .build();

        revisionRepository.save(anotherRevision);

        Optional<DocumentRevision> nextRevision = revisionRepository.findNextByDocumentAndVersion(revision.getDocument(), revision.getVersion());

        assertThat(nextRevision).isPresent();
    }

    @Test
    void shouldNotFindNextRevisionWhenDocumentIsInvalid() {
        Document document = Document.builder()
                                    .author(revision.getAuthor())
                                    .documentId("8d118708-9e09-4053-883d-8c2079f5c0a3")
                                    .version(1L)
                                    .name("dog.jpeg")
                                    .type("image/jpeg")
                                    .path("/test")
                                    .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                    .isArchived(false)
                                    .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .build();

        document = documentRepository.save(document);

        Optional<DocumentRevision> nextRevision = revisionRepository.findNextByDocumentAndVersion(document, revision.getVersion());

        assertThat(nextRevision).isEmpty();
    }

    @Test
    void shouldNotFindNextRevisionWhenRevisionIsTheOnlyOne() {
        Optional<DocumentRevision> nextRevision = revisionRepository.findNextByDocumentAndVersion(revision.getDocument(), revision.getVersion());

        assertThat(nextRevision).isEmpty();
    }

    @Test
    void shouldFindLastRevisionVersionByDocument() {
        Optional<Long> revisionVersion = revisionRepository.findLastRevisionVersionByDocument(revision.getDocument());

        assertThat(revisionVersion).isPresent()
                                   .contains(1L);
    }

    @Test
    void shouldFindLastRevisionVersionEqualToThreeWhenThreeRevisionsExist() {
        DocumentRevision firstRevision = DocumentRevision.builder()
                                                           .revisionId("57c00a2f-8435-4ea4-91d9-e195680dea37")
                                                           .author(revision.getAuthor())
                                                           .document(revision.getDocument())
                                                           .version(2L)
                                                           .name("dog.jpeg")
                                                           .type("image/jpeg")
                                                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                                                           .build();

        DocumentRevision secondRevision = DocumentRevision.builder()
                                                            .revisionId("8d118708-9e09-4053-883d-8c2079f5c0a3")
                                                            .author(revision.getAuthor())
                                                            .document(revision.getDocument())
                                                            .version(3L)
                                                            .name("dog.jpeg")
                                                            .type("image/jpeg")
                                                            .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                                            .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                                                            .build();

        revisionRepository.save(firstRevision);
        revisionRepository.save(secondRevision);

        Optional<Long> revisionVersion = revisionRepository.findLastRevisionVersionByDocument(revision.getDocument());

        assertThat(revisionVersion).isPresent()
                                   .contains(3L);
    }

    @Test
    void shouldNotFindRevisionVersionWhenDocumentIsInvalid() {
        Document document = Document.builder()
                                    .author(revision.getAuthor())
                                    .documentId("8d118708-9e09-4053-883d-8c2079f5c0a3")
                                    .version(1L)
                                    .name("dog.jpeg")
                                    .type("image/jpeg")
                                    .path("/test")
                                    .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                    .isArchived(false)
                                    .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .build();

        document = documentRepository.save(document);

        Optional<Long> revisionVersion = revisionRepository.findLastRevisionVersionByDocument(document);

        assertThat(revisionVersion).isEmpty();
    }

}