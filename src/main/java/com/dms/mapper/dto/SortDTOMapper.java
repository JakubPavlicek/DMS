package com.dms.mapper.dto;

import com.dms.dto.SortDTO;
import org.springframework.data.domain.Sort;

/**
 * The {@code SortDTOMapper} class is responsible for mapping {@link Sort} objects to {@link SortDTO} DTOs.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class SortDTOMapper {

    /** Private constructor to prevent instantiation of this utility class. */
    private SortDTOMapper() {
    }

    /**
     * Maps a {@link Sort} object to a {@link SortDTO} DTO.
     *
     * @param sort the {@link Sort} object to map
     * @return the mapped {@link SortDTO} DTO
     */
    public static SortDTO map(Sort sort) {
        return SortDTO.builder()
                      .empty(sort.isEmpty())
                      .sorted(sort.isSorted())
                      .unsorted(sort.isUnsorted())
                      .build();
    }

}
