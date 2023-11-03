package com.dms.mapper.dto;

import com.dms.dto.DocumentWithVersionDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;

public class DocumentWithVersionDTOMapper {

    public static DocumentWithVersionDTO map(Document document, DocumentRevision revision) {
        return DocumentWithVersionDTO.builder()
                                     .documentId(document.getDocumentId())
                                     .version(revision.getVersion())
                                     .author(UserDTOMapper.map(revision.getAuthor()))
                                     .name(revision.getName())
                                     .type(revision.getType())
                                     .path(revision.getPath())
                                     .createdAt(revision.getCreatedAt())
                                     .build();
    }

}
