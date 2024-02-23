package com.dms.mapper.dto;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.entity.DocumentRevision;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class PageWithRevisionsDTOMapper {

    private PageWithRevisionsDTOMapper() {
    }

    public static PageWithRevisionsDTO map(Page<DocumentRevision> revisionPage) {
        return PageWithRevisionsDTO.builder()
                                   .content(mapToDtoList(revisionPage))
                                   .pageable(PageableDTOMapper.map(revisionPage.getPageable()))
                                   .last(revisionPage.isLast())
                                   .totalElements(revisionPage.getTotalElements())
                                   .totalPages(revisionPage.getTotalPages())
                                   .first(revisionPage.isFirst())
                                   .size(revisionPage.getSize())
                                   .number(revisionPage.getNumber())
                                   .sort(SortDTOMapper.map(revisionPage.getSort()))
                                   .numberOfElements(revisionPage.getNumberOfElements())
                                   .empty(revisionPage.isEmpty())
                                   .build();
    }

    private static List<DocumentRevisionDTO> mapToDtoList(Page<DocumentRevision> revisionPage) {
        List<DocumentRevisionDTO> revisionDTOList = new ArrayList<>();
        revisionPage.forEach(revision -> revisionDTOList.add(DocumentRevisionDTOMapper.map(revision)));

        return revisionDTOList;
    }

}
