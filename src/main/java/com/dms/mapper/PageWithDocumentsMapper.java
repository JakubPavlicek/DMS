package com.dms.mapper;

import com.dms.dto.DocumentDTO;
import com.dms.dto.PageWithDocuments;
import org.springframework.data.domain.Page;

public class PageWithDocumentsMapper {

    public static PageWithDocuments map(Page<DocumentDTO> documentDtoPage) {
        return PageWithDocuments.builder()
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
