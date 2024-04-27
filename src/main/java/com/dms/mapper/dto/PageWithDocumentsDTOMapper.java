package com.dms.mapper.dto;

import com.dms.dto.DocumentDTO;
import com.dms.dto.PageWithDocumentsDTO;
import com.dms.entity.Document;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code PageWithDocumentsDTOMapper} class is responsible for mapping {@link Page} objects to {@link PageWithDocumentsDTO} DTOs.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class PageWithDocumentsDTOMapper {

    /** Private constructor to prevent instantiation of this utility class. */
    private PageWithDocumentsDTOMapper() {
    }

    /**
     * Maps a {@link Page} object to a {@link PageWithDocumentsDTO} DTO.
     *
     * @param documentPage the {@link Page} object to map
     * @return the mapped {@link PageWithDocumentsDTO} DTO
     */
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

    /**
     * Maps each {@link Document} entity in the {@link Page} to its corresponding {@link DocumentDTO}.
     *
     * @param documentPage the {@link Page} object containing the documents to map
     * @return a list of mapped {@link DocumentDTO} objects
     */
    private static List<DocumentDTO> mapToDtoList(Page<Document> documentPage) {
        List<DocumentDTO> documentDTOList = new ArrayList<>();
        documentPage.forEach(document -> documentDTOList.add(DocumentDTOMapper.map(document)));

        return documentDTOList;
    }

}
