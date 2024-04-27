package com.dms.mapper.dto;

import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.PageWithRevisionsDTO;
import com.dms.entity.DocumentRevision;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code PageWithRevisionsDTOMapper} class is responsible for mapping {@link Page} objects to {@link PageWithRevisionsDTO} DTOs.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class PageWithRevisionsDTOMapper {

    /** Private constructor to prevent instantiation of this utility class. */
    private PageWithRevisionsDTOMapper() {
    }

    /**
     * Maps a {@link Page} object to a {@link PageWithRevisionsDTO} DTO.
     *
     * @param revisionPage the {@link Page} object to map
     * @return the mapped {@link PageWithRevisionsDTO} DTO
     */
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

    /**
     * Maps each {@link DocumentRevision} entity in the {@link Page} to its corresponding {@link DocumentRevisionDTO}.
     *
     * @param revisionPage the {@link Page} object containing the revisions to map
     * @return a list of mapped {@link DocumentRevisionDTO} objects
     */
    private static List<DocumentRevisionDTO> mapToDtoList(Page<DocumentRevision> revisionPage) {
        List<DocumentRevisionDTO> revisionDTOList = new ArrayList<>();
        revisionPage.forEach(revision -> revisionDTOList.add(DocumentRevisionDTOMapper.map(revision)));

        return revisionDTOList;
    }

}
