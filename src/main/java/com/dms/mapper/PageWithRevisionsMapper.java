package com.dms.mapper;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithRevisions;
import org.springframework.data.domain.Page;

public class PageWithRevisionsMapper {

    public static PageWithRevisions map(Page<DocumentRevisionDTO> revisionDtoPage) {
        return PageWithRevisions.builder()
                                .content(revisionDtoPage.getContent())
                                .pageable(PageableDTOMapper.map(revisionDtoPage.getPageable()))
                                .last(revisionDtoPage.isLast())
                                .totalElements(revisionDtoPage.getTotalElements())
                                .totalPages(revisionDtoPage.getTotalPages())
                                .first(revisionDtoPage.isFirst())
                                .size(revisionDtoPage.getSize())
                                .number(revisionDtoPage.getNumber())
                                .sort(SortDTOMapper.map(revisionDtoPage.getSort()))
                                .numberOfElements(revisionDtoPage.getNumberOfElements())
                                .empty(revisionDtoPage.isEmpty())
                                .build();
    }

}
