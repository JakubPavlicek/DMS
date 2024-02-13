package com.dms.mapper.dto;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithRevisionsDTO;
import org.springframework.data.domain.Page;

public class PageWithRevisionsDTOMapper {

    private PageWithRevisionsDTOMapper()
    {
    }

    public static PageWithRevisionsDTO map(Page<DocumentRevisionDTO> revisionDtoPage) {
        return PageWithRevisionsDTO.builder()
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
