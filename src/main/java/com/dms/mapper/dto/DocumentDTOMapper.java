package com.dms.mapper.dto;

import com.dms.dto.DocumentDTO;
import com.dms.entity.Document;

public class DocumentDTOMapper {

    public static DocumentDTO map(Document document) {
        return DocumentDTO.builder()
                          .documentId(document.getDocumentId())
                          .author(UserDTOMapper.map(document.getAuthor()))
                          .version(document.getVersion())
                          .name(document.getName())
                          .type(document.getType())
                          .path(document.getPath())
                          .createdAt(document.getCreatedAt())
                          .updatedAt(document.getUpdatedAt())
                          .build();
    }

}
