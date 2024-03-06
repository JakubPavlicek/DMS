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
                           .size(20207L)
                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                           .isArchived(false)
                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                           .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                           .build();

        document = documentRepository.save(document);
    }

    @Test
    void shouldFindDocumentByDocumentIdAndAuthor() {
        Optional<Document> foundDocument = documentRepository.findByDocumentIdAndAuthor(document.getDocumentId(), document.getAuthor());

        assertThat(foundDocument).isPresent();
    }

    @Test
    void shouldNotFindDocumentByDocumentIdAndAuthorWhenDocumentIdIsInvalid() {
        Optional<Document> foundDocument = documentRepository.findByDocumentIdAndAuthor("ad9f1826-f847-4069-b57c-938b8d843c1d", document.getAuthor());

        assertThat(foundDocument).isEmpty();
    }

    @Test
    void shouldNotFindDocumentByDocumentIdAndAuthorWhenAuthorIsInvalid() {
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
    void shouldFindTwoDocumentsWhenTwoDocumentsWithSameAuthorExists() {
        Document anotherDocument = Document.builder()
                                           .author(author)
                                           .documentId("de5bebab-dc1c-4bc2-bf41-8039c076b63e")
                                           .version(1L)
                                           .name("dog.jpeg")
                                           .type("image/jpeg")
                                           .path("/test")
                                           .size(20207L)
                                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                           .isArchived(false)
                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .build();

        documentRepository.save(anotherDocument);

        int documentCount = documentRepository.findAllByAuthor(document.getAuthor()).size();

        assertThat(documentCount).isEqualTo(2);
    }

    @Test
    void shouldNotFindDocumentsWhenAuthorIsInvalid() {
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
    void shouldReturnArchivedDocumentCount() {
        Document anotherDocument = Document.builder()
                                           .author(author)
                                           .documentId("de5bebab-dc1c-4bc2-bf41-8039c076b63e")
                                           .version(1L)
                                           .name("dog.jpeg")
                                           .type("image/jpeg")
                                           .path("/test")
                                           .size(20207L)
                                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                           .isArchived(true)
                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .build();

        documentRepository.save(anotherDocument);

        Integer archivedDocumentCount = documentRepository.countAllByIsArchived(true);

        assertThat(archivedDocumentCount).isEqualTo(1);
    }

    @Test
    void shouldReturnTrueWhenTwoEqualHashesExist() {
        Document anotherDocument = Document.builder()
                                           .author(author)
                                           .documentId("de5bebab-dc1c-4bc2-bf41-8039c076b63e")
                                           .version(1L)
                                           .name("dog.jpeg")
                                           .type("image/jpeg")
                                           .path("/test")
                                           .size(20207L)
                                           .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                           .isArchived(false)
                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .build();

        documentRepository.save(anotherDocument);

        boolean duplicateHashExists = documentRepository.duplicateHashExists(document.getHash());

        assertThat(duplicateHashExists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoDuplicateHashesExist() {
        Document anotherDocument = Document.builder()
                                           .author(author)
                                           .documentId("de5bebab-dc1c-4bc2-bf41-8039c076b63e")
                                           .version(1L)
                                           .name("dog.jpeg")
                                           .type("image/jpeg")
                                           .path("/test")
                                           .size(20207L)
                                           .hash("04cda4ddf5773cbc4f80452696112091f60380015b17973aae8a11f3d92e7c7d")
                                           .isArchived(false)
                                           .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                           .build();

        documentRepository.save(anotherDocument);

        boolean duplicateHashExists = documentRepository.duplicateHashExists(document.getHash());

        assertThat(duplicateHashExists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenDocumentExistsAtPathForGivenAuthor() {
        boolean duplicateDocumentExists = documentRepository.documentWithPathAlreadyExists(document.getName(), document.getPath(), document.getAuthor());

        assertThat(duplicateDocumentExists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTwoDistinctDocumentNamesWithSamePathAndAuthorExist() {
        boolean duplicateDocumentExists = documentRepository.documentWithPathAlreadyExists("cat.jpeg", document.getPath(), document.getAuthor());

        assertThat(duplicateDocumentExists).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTwoDistinctPathsWithSameDocumentNameAndAuthorExist() {
        boolean duplicateDocumentExists = documentRepository.documentWithPathAlreadyExists(document.getName(), "/home", document.getAuthor());

        assertThat(duplicateDocumentExists).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTwoDistinctAuthorsOfTheSameDocumentExist() {
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