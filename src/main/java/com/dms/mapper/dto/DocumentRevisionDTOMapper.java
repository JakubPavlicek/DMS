package com.dms.mapper.dto;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.entity.DocumentRevision;

public class DocumentRevisionDTOMapper {

    private DocumentRevisionDTOMapper() {
    }

    public static DocumentRevisionDTO map(DocumentRevision revision) {
        return DocumentRevisionDTO.builder()
                                  .revisionId(revision.getRevisionId())
                                  .author(UserDTOMapper.mapToUserDTO(revision.getAuthor()))
                                  .version(revision.getVersion())
                                  .name(revision.getName())
                                  .type(revision.getType())
                                  .createdAt(revision.getCreatedAt())
                                  .build();
    }

}
