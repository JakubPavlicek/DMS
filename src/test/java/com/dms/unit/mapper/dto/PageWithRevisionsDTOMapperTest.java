package com.dms.unit.mapper.dto;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.mapper.dto.DocumentRevisionDTOMapper;
import com.dms.mapper.dto.PageWithRevisionsDTOMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageWithRevisionsDTOMapperTest {

    @Test
    void shouldReturnPageWithRevisionsDTO() {
        User author = User.builder()
                          .userId("fde1be20-54b1-41f6-8506-bdd0d63c189f")
                          .name("james")
                          .email("james@gmail.com")
                          .password("secret123!")
                          .build();

        Document document = Document.builder()
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

        DocumentRevision revision = DocumentRevision.builder()
                                   .revisionId("95f6dbc2-b919-4b04-94b6-e857a92677d4")
                                   .author(author)
                                   .document(document)
                                   .version(1L)
                                   .name("dog.jpeg")
                                   .type("image/jpeg")
                                   .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                   .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                                   .build();

        DocumentRevision revision2 = DocumentRevision.builder()
                                                    .revisionId("83bf9967-25f5-42a6-a84b-c6882992f15d")
                                                    .author(author)
                                                    .document(document)
                                                    .version(2L)
                                                    .name("dog.jpeg")
                                                    .type("image/jpeg")
                                                    .hash("fb1c43900e39c38a20d84bdc3dd87d798b43c293a4ff243f2cc27b267f1efa58")
                                                    .createdAt(LocalDateTime.parse("2023-11-14T08:30:01"))
                                                    .build();

        List<DocumentRevision> revisionList = List.of(revision, revision2);

        List<DocumentRevisionDTO> revisionDTOList = new ArrayList<>();
        revisionList.forEach(rev -> revisionDTOList.add(DocumentRevisionDTOMapper.map(rev)));

        Pageable pageable = Pageable.ofSize(revisionDTOList.size());

        Page<DocumentRevisionDTO> revisionPage = new PageImpl<>(revisionDTOList, pageable, revisionDTOList.size());

        PageWithRevisionsDTO pageWithDocumentsDTO = PageWithRevisionsDTOMapper.map(revisionPage);

        assertThat(pageWithDocumentsDTO.getContent()).hasSize(revisionPage.getContent().size());
        assertThat(pageWithDocumentsDTO.getPageable()).isNotNull();
        assertThat(pageWithDocumentsDTO.getLast()).isEqualTo(revisionPage.isLast());
        assertThat(pageWithDocumentsDTO.getTotalElements()).isEqualTo(revisionPage.getTotalElements());
        assertThat(pageWithDocumentsDTO.getTotalPages()).isEqualTo(revisionPage.getTotalPages());
        assertThat(pageWithDocumentsDTO.getFirst()).isEqualTo(revisionPage.isFirst());
        assertThat(pageWithDocumentsDTO.getSize()).isEqualTo(revisionPage.getSize());
        assertThat(pageWithDocumentsDTO.getNumber()).isEqualTo(revisionPage.getNumber());
        assertThat(pageWithDocumentsDTO.getSort()).isNotNull();
        assertThat(pageWithDocumentsDTO.getNumberOfElements()).isEqualTo(revisionPage.getNumberOfElements());
        assertThat(pageWithDocumentsDTO.getEmpty()).isEqualTo(revisionPage.isEmpty());
    }

}