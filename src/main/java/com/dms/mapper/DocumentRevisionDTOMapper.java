package com.dms.mapper;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.entity.DocumentRevision;

public class DocumentRevisionDTOMapper {

    public static DocumentRevisionDTO map(DocumentRevision revision) {
        return DocumentRevisionDTO.builder()
                                  .revisionId(revision.getRevisionId())
                                  .author(UserDTOMapper.map(revision.getAuthor()))
                                  .name(revision.getName())
                                  .type(revision.getType())
                                  .path(revision.getPath())
                                  .createdAt(revision.getCreatedAt())
                                  .build();
    }

}
