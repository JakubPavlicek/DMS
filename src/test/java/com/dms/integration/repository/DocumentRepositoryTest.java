package com.dms.integration.repository;

import com.dms.entity.Document;
import com.dms.entity.User;
import com.dms.repository.DocumentRepository;
import com.dms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    private Document document;

    @BeforeEach
    void setUp() {
        User author = createUser();
        author = userRepository.save(author);

        document = createDocument(author);
        document = documentRepository.save(document);
    }

    @Test
    void shouldFindDocumentByDocumentIdAndAuthor() {
        assertThat(documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), document.getAuthor())).isPresent();
    }

    @Test
    void shouldReturnEmptyOptionalWhenDocumentNotFoundByAuthorAndInvalidDocumentId() {
        String documentId = UUID.randomUUID().toString();
        assertThat(documentRepository.findByDocumentIdAndAuthor(documentId, document.getAuthor())).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenDocumentNotFoundByDocumentIdAndInvalidAuthor() {
        User author = createUser();
        author.setEmail("john@gmail.com");
        author = userRepository.save(author);

        assertThat(documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), author)).isEmpty();
    }

    @Test
    void shouldFindAllDocumentsByAuthor() {
        Document anotherDocument = createDocument(document.getAuthor());
        documentRepository.save(anotherDocument);

        assertThat(documentRepository.findAllByAuthor(document.getAuthor()).size()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoDocumentsFoundForAuthor() {
        User author = createUser();
        author.setEmail("john@gmail.com");
        author = userRepository.save(author);

        assertThat(documentRepository.findAllByAuthor(author)).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenDuplicateHashExists() {
        Document anotherDocument = createDocument(document.getAuthor());
        documentRepository.save(anotherDocument);

        assertThat(documentRepository.duplicateHashExists(document.getHash())).isTrue();
    }

    @Test
    void shouldReturnFalseWhenDuplicateHashDoesNotExist() {
        Document anotherDocument = createDocument(document.getAuthor());
        anotherDocument.setHash("04cda4ddf5773cbc4f80452696112091f60380015b17973aae8a11f3d92e7c7d");
        documentRepository.save(anotherDocument);

        assertThat(documentRepository.duplicateHashExists(document.getHash())).isFalse();
    }

    @Test
    void shouldReturnTrueWhenDocumentWithPathAlreadyExists() {
        assertThat(documentRepository.documentWithPathAlreadyExists(document.getName(), document.getPath(), document.getAuthor())).isTrue();
    }

    @Test
    void shouldReturnFalseWhenDocumentWithDifferentNameAndSamePathDoesNotExist() {
        assertThat(documentRepository.documentWithPathAlreadyExists("cat.jpeg", document.getPath(), document.getAuthor())).isFalse();
    }

    @Test
    void shouldReturnFalseWhenDocumentWithDifferentPathAndSameNameDoesNotExist() {
        assertThat(documentRepository.documentWithPathAlreadyExists(document.getName(), "/home", document.getAuthor())).isFalse();
    }

    @Test
    void shouldReturnFalseWhenDocumentWithDifferentAuthorDoesNotExist() {
        User author = createUser();
        author.setEmail("john@gmail.com");
        author = userRepository.save(author);

        assertThat(documentRepository.documentWithPathAlreadyExists(document.getName(), document.getPath(), author)).isFalse();
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

}