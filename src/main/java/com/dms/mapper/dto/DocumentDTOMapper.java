package com.dms.mapper.dto;

import com.dms.dto.DocumentDTO;
import com.dms.entity.Document;

public class DocumentDTOMapper {

    private DocumentDTOMapper() {
    }

    public static DocumentDTO map(Document document) {
        return DocumentDTO.builder()
                          .documentId(document.getDocumentId())
                          .author(UserDTOMapper.mapToUserDTO(document.getAuthor()))
                          .version(document.getVersion())
                          .name(document.getName())
                          .type(document.getType())
                          .size(document.getSize())
                          .path(document.getPath())
                          .isArchived(document.getIsArchived())
                          .createdAt(document.getCreatedAt())
                          .updatedAt(document.getUpdatedAt())
                          .deleteAt(document.getDeleteAt())
                          .build();
    }

}
