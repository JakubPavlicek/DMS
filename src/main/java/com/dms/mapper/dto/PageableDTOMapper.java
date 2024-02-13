package com.dms.mapper.dto;

import com.dms.dto.PageableDTO;
import org.springframework.data.domain.Pageable;

public class PageableDTOMapper {

    private PageableDTOMapper()
    {
    }

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
