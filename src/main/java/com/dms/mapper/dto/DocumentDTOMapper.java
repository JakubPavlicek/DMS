package com.dms.mapper.dto;

import com.dms.dto.DocumentDTO;
import com.dms.entity.Document;

/**
 * The {@code DocumentDTOMapper} class is responsible for mapping {@link Document} entities to {@link DocumentDTO} DTOs.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class DocumentDTOMapper {

    /** Private constructor to prevent instantiation of this utility class. */
    private DocumentDTOMapper() {
    }

    /**
     * Maps a {@link Document} entity to a {@link DocumentDTO} DTO.
     *
     * @param document the {@link Document} entity to map
     * @return the mapped {@link DocumentDTO} DTO
     */
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
