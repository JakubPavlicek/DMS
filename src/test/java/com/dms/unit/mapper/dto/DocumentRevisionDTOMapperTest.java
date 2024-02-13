package com.dms.unit.mapper.dto;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.mapper.dto.DocumentRevisionDTOMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class DocumentRevisionDTOMapperTest {

    @Test
    void whenValidDocumentRevision_thenReturnDocumentRevisionDTO() {
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

        DocumentRevisionDTO revisionDTO = DocumentRevisionDTOMapper.map(revision);

        assertThat(revisionDTO.getRevisionId()).isEqualTo(revision.getRevisionId());
        assertThat(revisionDTO.getAuthor()).isNotNull();
        assertThat(revisionDTO.getVersion()).isEqualTo(revision.getVersion());
        assertThat(revisionDTO.getName()).isEqualTo(revision.getName());
        assertThat(revisionDTO.getType()).isEqualTo(revision.getType());
        assertThat(revisionDTO.getCreatedAt()).isEqualTo(revision.getCreatedAt());
    }

}