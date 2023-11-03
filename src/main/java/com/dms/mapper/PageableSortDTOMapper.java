package com.dms.mapper;

import com.dms.dto.PageableSortDTO;
import org.springframework.data.domain.Sort;

public class PageableSortDTOMapper {

    public static PageableSortDTO map(Sort sort) {
        return PageableSortDTO.builder()
                      .empty(sort.isEmpty())
                      .sorted(sort.isSorted())
                      .unsorted(sort.isUnsorted())
                      .build();
    }

}
