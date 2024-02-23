package com.dms.mapper.dto;

import com.dms.dto.DocumentDTO;
import com.dms.dto.PageWithDocumentsDTO;
import com.dms.entity.Document;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class PageWithDocumentsDTOMapper {

    private PageWithDocumentsDTOMapper() {
    }

    public static PageWithDocumentsDTO map(Page<Document> documentPage) {
        return PageWithDocumentsDTO.builder()
                                   .content(mapToDtoList(documentPage))
                                   .pageable(PageableDTOMapper.map(documentPage.getPageable()))
                                   .last(documentPage.isLast())
                                   .totalElements(documentPage.getTotalElements())
                                   .totalPages(documentPage.getTotalPages())
                                   .first(documentPage.isFirst())
                                   .size(documentPage.getSize())
                                   .number(documentPage.getNumber())
                                   .sort(SortDTOMapper.map(documentPage.getSort()))
                                   .numberOfElements(documentPage.getNumberOfElements())
                                   .empty(documentPage.isEmpty())
                                   .build();
    }

    private static List<DocumentDTO> mapToDtoList(Page<Document> documentPage) {
        List<DocumentDTO> documentDTOList = new ArrayList<>();
        documentPage.forEach(document -> documentDTOList.add(DocumentDTOMapper.map(document)));

        return documentDTOList;
    }

}
