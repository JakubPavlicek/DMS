package com.dms.unit.mapper.dto;

import com.dms.dto.PageWithDocumentsDTO;
import com.dms.entity.Document;
import com.dms.entity.Role;
import com.dms.entity.User;
import com.dms.mapper.dto.PageWithDocumentsDTOMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageWithDocumentsDTOMapperTest {

    @Test
    void shouldReturnPageWithDocumentsDTO() {
        User author = User.builder()
                          .userId("fde1be20-54b1-41f6-8506-bdd0d63c189f")
                          .name("james")
                          .email("james@gmail.com")
                          .password("secret123!")
                          .role(Role.USER)
                          .build();

        Document firstDocument = Document.builder()
                                    .author(author)
                                    .documentId("277f6b39-ec44-4fbe-9605-8d0dee790518")
                                    .version(1L)
                                    .name("dog.jpeg")
                                    .type("image/jpeg")
                                    .path("/test")
                                    .size(1413L)
                                    .isArchived(false)
                                    .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                    .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                    .deleteAt(null)
                                    .build();

        Document secondDocument = Document.builder()
                                     .author(author)
                                     .documentId("ce234e8d-a809-437b-8198-c5faa07f9ff1")
                                     .version(1L)
                                     .name("cat.jpeg")
                                     .type("image/jpeg")
                                     .path("/test")
                                     .size(1413L)
                                     .isArchived(false)
                                     .hash("0a6df48a27ef7db48b213df3dba84fad1bb4fd9b47568da1f570c067d9a4867f")
                                     .createdAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                     .updatedAt(LocalDateTime.parse("2023-11-14T08:30:00"))
                                     .deleteAt(null)
                                     .build();

        List<Document> documentList = List.of(firstDocument, secondDocument);
        Pageable pageable = Pageable.ofSize(documentList.size());

        Page<Document> documentPage = new PageImpl<>(documentList, pageable, documentList.size());

        PageWithDocumentsDTO pageWithDocumentsDTO = PageWithDocumentsDTOMapper.map(documentPage);

        assertThat(pageWithDocumentsDTO.getContent()).hasSize(documentPage.getContent().size());
        assertThat(pageWithDocumentsDTO.getPageable()).isNotNull();
        assertThat(pageWithDocumentsDTO.getLast()).isEqualTo(documentPage.isLast());
        assertThat(pageWithDocumentsDTO.getTotalElements()).isEqualTo(documentPage.getTotalElements());
        assertThat(pageWithDocumentsDTO.getTotalPages()).isEqualTo(documentPage.getTotalPages());
        assertThat(pageWithDocumentsDTO.getFirst()).isEqualTo(documentPage.isFirst());
        assertThat(pageWithDocumentsDTO.getSize()).isEqualTo(documentPage.getSize());
        assertThat(pageWithDocumentsDTO.getNumber()).isEqualTo(documentPage.getNumber());
        assertThat(pageWithDocumentsDTO.getSort()).isNotNull();
        assertThat(pageWithDocumentsDTO.getNumberOfElements()).isEqualTo(documentPage.getNumberOfElements());
        assertThat(pageWithDocumentsDTO.getEmpty()).isEqualTo(documentPage.isEmpty());
    }

}