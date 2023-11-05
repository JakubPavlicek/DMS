package com.dms.mapper.dto;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.entity.DocumentRevision;

public class DocumentRevisionDTOMapper {

    public static DocumentRevisionDTO map(DocumentRevision revision) {
        return DocumentRevisionDTO.builder()
                                  .revisionId(revision.getRevisionId())
                                  .author(UserDTOMapper.mapToUserDTO(revision.getAuthor()))
                                  .name(revision.getName())
                                  .type(revision.getType())
                                  .path(revision.getPath())
                                  .createdAt(revision.getCreatedAt())
                                  .build();
    }

}
