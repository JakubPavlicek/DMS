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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private Document document;

    @BeforeEach
    void setUp() {
        author = User.builder()
                     .userId("6ab79b7e-4cb0-481c-8fc0-0e40c5bd076b")
                     .name("james")
                     .email("james@gmail.com")
                     .password("secret123!")
                     .build();

        author = userRepository.save(author);

        document = Document.builder()
                           .author(author)
                           .documentId("3195ce96-c5c6-447c-9437-55d8d6fcf785")
                           .version(1L)
                           .name("dog.jpeg")
                           .type("image/jpeg")
                           .path("/test")
                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                           .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                           .build();

        document = documentRepository.save(document);
    }

    @Test
    void whenValidDocumentIdAndAuthor_thenDocumentShouldBeFound() {
        Optional<Document> foundDocument = documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), document.getAuthor());

        assertThat(foundDocument).isPresent();
    }

    @Test
    void whenInvalidDocumentIdAndValidAuthor_thenNoDocumentShouldBeFound() {
        Optional<Document> foundDocument = documentRepository.findByDocumentIdAndAuthor("ad9f1826-f847-4069-b57c-938b8d843c1d", document.getAuthor());

        assertThat(foundDocument).isEmpty();
    }

    @Test
    void whenInvalidAuthorAndValidDocumentId_thenNoDocumentShouldBeFound() {
        User anotherAuthor = User.builder()
                                 .userId("ad9f1826-f847-4069-b57c-938b8d843c1d")
                                 .name("john")
                                 .email("john@gmail.com")
                                 .password("password123!")
                                 .build();

        anotherAuthor = userRepository.save(anotherAuthor);

        Optional<Document> foundDocument = documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), anotherAuthor);

        assertThat(foundDocument).isEmpty();
    }

    @Test
    void whenTwoDocumentsWithSameAuthorExists_thenTwoDocumentsShouldBeFound() {
        Document anotherDocument = Document.builder()
                                           .author(author)
                                           .documentId("de5bebab-dc1c-4bc2-bf41-8039c076b63e")
                                           .version(1L)
                                           .name("dog.jpeg")
                                           .type("image/jpeg")
                                           .path("/test")
                                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .build();

        documentRepository.save(anotherDocument);

        int documentCount = documentRepository.findAllByAuthor(document.getAuthor()).size();

        assertThat(documentCount).isEqualTo(2);
    }

    @Test
    void whenInvalidAuthor_thenNoDocumentsShouldBeFound() {
        User anotherAuthor = User.builder()
                                 .userId("ad9f1826-f847-4069-b57c-938b8d843c1d")
                                 .name("john")
                                 .email("john@gmail.com")
                                 .password("password123!")
                                 .build();

        anotherAuthor = userRepository.save(anotherAuthor);

        List<Document> documents = documentRepository.findAllByAuthor(anotherAuthor);

        assertThat(documents).isEmpty();
    }

    @Test
    void whenTwoEqualHashesExist_thenShouldReturnTrue() {
        Document anotherDocument = Document.builder()
                                           .author(author)
                                           .documentId("de5bebab-dc1c-4bc2-bf41-8039c076b63e")
                                           .version(1L)
                                           .name("dog.jpeg")
                                           .type("image/jpeg")
                                           .path("/test")
                                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .build();

        documentRepository.save(anotherDocument);

        boolean duplicateHashExists = documentRepository.duplicateHashExists(document.getHash());

        assertThat(duplicateHashExists).isTrue();
    }

    @Test
    void whenNoDuplicateHashesExist_thenShouldReturnFalse() {
        Document anotherDocument = Document.builder()
                                           .author(author)
                                           .documentId("de5bebab-dc1c-4bc2-bf41-8039c076b63e")
                                           .version(1L)
                                           .name("dog.jpeg")
                                           .type("image/jpeg")
                                           .path("/test")
                                           .hash("04cda4ddf5773cbc4f80452696112091f60380015b17973aae8a11f3d92e7c7d")
                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .build();

        documentRepository.save(anotherDocument);

        boolean duplicateHashExists = documentRepository.duplicateHashExists(document.getHash());

        assertThat(duplicateHashExists).isFalse();
    }

    @Test
    void whenDocumentExistsAtPathForGivenAuthor_thenShouldReturnTrue() {
        boolean duplicateDocumentExists = documentRepository.documentWithPathAlreadyExists(document.getName(), document.getPath(), document.getAuthor());

        assertThat(duplicateDocumentExists).isTrue();
    }

    @Test
    void whenTwoDistinctDocumentNamesWithSamePathAndAuthorExist_thenShouldReturnFalse() {
        boolean duplicateDocumentExists = documentRepository.documentWithPathAlreadyExists("cat.jpeg", document.getPath(), document.getAuthor());

        assertThat(duplicateDocumentExists).isFalse();
    }

    @Test
    void whenTwoDistinctPathsWithSameDocumentNameAndAuthorExist_thenShouldReturnFalse() {
        boolean duplicateDocumentExists = documentRepository.documentWithPathAlreadyExists(document.getName(), "/home", document.getAuthor());

        assertThat(duplicateDocumentExists).isFalse();
    }

    @Test
    void whenTwoDistinctAuthorsOfTheSameDocumentExist_thenShouldReturnFalse() {
        User anotherAuthor = User.builder()
                                 .userId("ad9f1826-f847-4069-b57c-938b8d843c1d")
                                 .name("john")
                                 .email("john@gmail.com")
                                 .password("password123!")
                                 .build();

        anotherAuthor = userRepository.save(anotherAuthor);

        boolean documentExists = documentRepository.documentWithPathAlreadyExists(document.getName(), document.getPath(), anotherAuthor);

        assertThat(documentExists).isFalse();
    }

}