package com.dms.unit.mapper.dto;

import com.dms.dto.DocumentDTO;
import com.dms.entity.Document;
import com.dms.entity.User;
import com.dms.mapper.dto.DocumentDTOMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class DocumentDTOMapperTest {

    @Test
    void shouldReturnDocumentDTO() {
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

        DocumentDTO documentDTO = DocumentDTOMapper.map(document);

        assertThat(documentDTO.getDocumentId()).isEqualTo(document.getDocumentId());
        assertThat(documentDTO.getAuthor()).isNotNull();
        assertThat(documentDTO.getVersion()).isEqualTo(document.getVersion());
        assertThat(documentDTO.getName()).isEqualTo(document.getName());
        assertThat(documentDTO.getType()).isEqualTo(document.getType());
        assertThat(documentDTO.getPath()).isEqualTo(document.getPath());
        assertThat(documentDTO.getCreatedAt()).isEqualTo(document.getCreatedAt());
        assertThat(documentDTO.getUpdatedAt()).isEqualTo(document.getUpdatedAt());
    }

}