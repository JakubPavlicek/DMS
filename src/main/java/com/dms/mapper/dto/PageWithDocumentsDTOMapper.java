package com.dms.mapper.dto;

import com.dms.dto.DocumentDTO;
import com.dms.dto.PageWithDocumentsDTO;
import org.springframework.data.domain.Page;

public class PageWithDocumentsDTOMapper {

    private PageWithDocumentsDTOMapper()
    {
    }

    public static PageWithDocumentsDTO map(Page<DocumentDTO> documentDtoPage) {
        return PageWithDocumentsDTO.builder()
                                   .content(documentDtoPage.getContent())
                                   .pageable(PageableDTOMapper.map(documentDtoPage.getPageable()))
                                   .last(documentDtoPage.isLast())
                                   .totalElements(documentDtoPage.getTotalElements())
                                   .totalPages(documentDtoPage.getTotalPages())
                                   .first(documentDtoPage.isFirst())
                                   .size(documentDtoPage.getSize())
                                   .number(documentDtoPage.getNumber())
                                   .sort(SortDTOMapper.map(documentDtoPage.getSort()))
                                   .numberOfElements(documentDtoPage.getNumberOfElements())
                                   .empty(documentDtoPage.isEmpty())
                                   .build();
    }

}
