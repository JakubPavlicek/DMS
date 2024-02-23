package com.dms.mapper.dto;

import com.dms.dto.SortDTO;
import org.springframework.data.domain.Sort;

public class SortDTOMapper {

    private SortDTOMapper() {
    }

    public static SortDTO map(Sort sort) {
        return SortDTO.builder()
                      .empty(sort.isEmpty())
                      .sorted(sort.isSorted())
                      .unsorted(sort.isUnsorted())
                      .build();
    }

}
