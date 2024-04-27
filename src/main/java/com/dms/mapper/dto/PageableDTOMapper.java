package com.dms.mapper.dto;

import com.dms.dto.PageableDTO;
import org.springframework.data.domain.Pageable;

/**
 * The {@code PageableDTOMapper} class is responsible for mapping {@link Pageable} objects to {@link PageableDTO} DTOs.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class PageableDTOMapper {

    /** Private constructor to prevent instantiation of this utility class. */
    private PageableDTOMapper() {
    }

    /**
     * Maps a {@link Pageable} object to a {@link PageableDTO} DTO.
     *
     * @param pageable the {@link Pageable} object to map
     * @return the mapped {@link PageableDTO} DTO
     */
    public static PageableDTO map(Pageable pageable) {
        return PageableDTO.builder()
                          .pageNumber(pageable.getPageNumber())
                          .pageSize(pageable.getPageSize())
                          .sort(SortDTOMapper.map(pageable.getSort()))
                          .offset(pageable.getOffset())
                          .paged(pageable.isPaged())
                          .unpaged(pageable.isUnpaged())
                          .build();
    }

}
