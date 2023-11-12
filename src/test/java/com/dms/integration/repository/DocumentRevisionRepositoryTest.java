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
import java.util.UUID;

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
    void shouldFindRevisionByRevisionIdAndAuthor() {
        assertThat(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), revision.getAuthor())).isPresent();
    }

    @Test
    void shouldReturnEmptyOptionalWhenRevisionNotFoundByAuthorAndInvalidRevisionId() {
        String revisionId = UUID.randomUUID()
                                .toString();
        assertThat(revisionRepository.findByRevisionIdAndAuthor(revisionId, revision.getAuthor())).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenRevisionNotFoundByRevisionIdAndInvalidAuthor() {
        User author = createUser();
        author.setEmail("john@email.com");
        author = userRepository.save(author);

        assertThat(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), author)).isEmpty();
    }

    @Test
    void shouldFindRevisionByDocumentAndRevisionId() {
        assertThat(revisionRepository.findByDocumentAndRevisionId(revision.getDocument(), revision.getRevisionId())).isPresent();
    }

    @Test
    void shouldReturnEmptyOptionalWhenRevisionNotFoundByDocumentAndInvalidRevisionId() {
        String revisionId = UUID.randomUUID()
                                .toString();
        assertThat(revisionRepository.findByDocumentAndRevisionId(revision.getDocument(), revisionId)).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenRevisionNotFoundByRevisionIdAndInvalidDocument() {
        User author = createUser();
        author.setEmail("john@gmail.com");
        author = userRepository.save(author);

        Document document = createDocument(author);
        document = documentRepository.save(document);

        assertThat(revisionRepository.findByDocumentAndRevisionId(document, revision.getRevisionId())).isEmpty();
    }

    @Test
    void shouldReturnRevisionsByDocumentOrderByCreatedAtAsc() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.findAllByDocumentOrderByCreatedAtAsc(revision.getDocument())
                                     .size()).isEqualTo(2);
    }

    @Test
    void shouldReturnRevisionsOrderedByCreatedAtAscByDocument() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setCreatedAt(LocalDateTime.now()
                                                  .minusHours(1));
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.findAllByDocumentOrderByCreatedAtAsc(revision.getDocument())).extracting(DocumentRevision::getCreatedAt)
                                                                                                   .isSortedAccordingTo(Comparator.naturalOrder());
    }

    @Test
    void shouldDeleteRevisionByRevisionId() {
        revisionRepository.deleteByRevisionId(revision.getRevisionId());
        assertThat(revisionRepository.findByRevisionIdAndAuthor(revision.getRevisionId(), revision.getAuthor())).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenDuplicateHashExists() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.duplicateHashExists(revision.getHash())).isTrue();
    }

    @Test
    void shouldReturnFalseWhenDuplicateHashDoesNotExist() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setHash("04cda4ddf5773cbc4f80452696112091f60380015b17973aae8a11f3d92e7c7d");
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.duplicateHashExists(revision.getHash())).isFalse();
    }

    @Test
    void shouldFindPreviousRevisionByDocumentAndVersion() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setVersion(revision.getVersion() + 1);
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.findPreviousByDocumentAndVersion(revision.getDocument(), anotherRevision.getVersion())).isPresent();
    }

    @Test
    void shouldReturnEmptyOptionalWhenPreviousRevisonDoesNotExistByInvalidDocument() {
        Document document = createDocument(revision.getAuthor());
        document = documentRepository.save(document);

        assertThat(revisionRepository.findPreviousByDocumentAndVersion(document, revision.getVersion())).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenPreviousRevisionByDocumentAndVersionDoesNotExist() {
        assertThat(revisionRepository.findPreviousByDocumentAndVersion(revision.getDocument(), revision.getVersion())).isEmpty();
    }

    @Test
    void shouldFindNextRevisionByDocumentAndVersion() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setVersion(revision.getVersion() + 1);
        revisionRepository.save(anotherRevision);

        assertThat(revisionRepository.findNextByDocumentAndVersion(revision.getDocument(), revision.getVersion())).isPresent();
    }

    @Test
    void shouldReturnEmptyOptionalWhenNextRevisonDoesNotExistByInvalidDocument() {
        Document document = createDocument(revision.getAuthor());
        document = documentRepository.save(document);

        assertThat(revisionRepository.findNextByDocumentAndVersion(document, revision.getVersion())).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenNextRevisionByDocumentAndVersionDoesNotExist() {
        assertThat(revisionRepository.findNextByDocumentAndVersion(revision.getDocument(), revision.getVersion())).isEmpty();
    }

    @Test
    void shouldFindLastRevisionVersionByDocument() {
        assertThat(revisionRepository.findLastRevisionVersionByDocument(revision.getDocument())).isPresent();
    }

    @Test
    void shouldFindLastRevisionVersionWithMultipleRevisionsByDocument() {
        DocumentRevision anotherRevision = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision.setVersion(revision.getVersion() + 1);
        revisionRepository.save(anotherRevision);

        DocumentRevision anotherRevision2 = createRevision(revision.getAuthor(), revision.getDocument());
        anotherRevision2.setVersion(anotherRevision.getVersion() + 1);
        revisionRepository.save(anotherRevision2);

        assertThat(revisionRepository.findLastRevisionVersionByDocument(revision.getDocument()).get()).isEqualTo(3);
    }

    @Test
    void shouldNotFindLastRevisionVersionWhenDocumentIsInvalid() {
        Document document = createDocument(revision.getAuthor());
        document = documentRepository.save(document);

        assertThat(revisionRepository.findLastRevisionVersionByDocument(document)).isEmpty();
    }

    private DocumentRevision createRevision(User author, Document document) {
        return DocumentRevision.builder()
                               .revisionId(UUID.randomUUID()
                                               .toString())
                               .author(author)
                               .document(document)
                               .version(1L)
                               .name("dog.jpeg")
                               .type("image/jpeg")
                               .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                               .createdAt(LocalDateTime.now())
                               .build();
    }

    private Document createDocument(User author) {
        return Document.builder()
                       .author(author)
                       .documentId(UUID.randomUUID()
                                       .toString())
                       .version(1L)
                       .name("dog.jpeg")
                       .type("image/jpeg")
                       .path("/test")
                       .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                       .createdAt(LocalDateTime.now())
                       .updatedAt(LocalDateTime.now())
                       .build();
    }

    private User createUser() {
        return User.builder()
                   .userId(UUID.randomUUID()
                               .toString())
                   .name("james")
                   .email("james@gmail.com")
                   .password("secret123!")
                   .build();
    }


}