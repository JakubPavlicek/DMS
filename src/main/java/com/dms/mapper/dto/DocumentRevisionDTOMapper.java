package com.dms.mapper.dto;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.entity.DocumentRevision;

/**
 * The {@code DocumentRevisionDTOMapper} class is responsible for mapping {@link DocumentRevision} entities to {@link DocumentRevisionDTO} DTOs.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class DocumentRevisionDTOMapper {

    /** Private constructor to prevent instantiation of this utility class. */
    private DocumentRevisionDTOMapper() {
    }

    /**
     * Maps a {@link DocumentRevision} entity to a {@link DocumentRevisionDTO} DTO.
     *
     * @param revision the {@link DocumentRevision} entity to map
     * @return the mapped {@link DocumentRevisionDTO} DTO
     */
    public static DocumentRevisionDTO map(DocumentRevision revision) {
        return DocumentRevisionDTO.builder()
                                  .revisionId(revision.getRevisionId())
                                  .author(UserDTOMapper.mapToUserDTO(revision.getAuthor()))
                                  .version(revision.getVersion())
                                  .name(revision.getName())
                                  .type(revision.getType())
                                  .size(revision.getSize())
                                  .createdAt(revision.getCreatedAt())
                                  .build();
    }

}
