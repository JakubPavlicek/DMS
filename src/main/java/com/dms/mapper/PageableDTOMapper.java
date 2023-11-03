package com.dms.mapper;

import com.dms.dto.PageableDTO;
import org.springframework.data.domain.Pageable;

public class PageableDTOMapper {

    public static PageableDTO map(Pageable pageable) {
        return PageableDTO.builder()
                          .pageNumber(pageable.getPageNumber())
                          .pageSize(pageable.getPageSize())
                          .sort(PageableSortDTOMapper.map(pageable.getSort()))
                          .offset(pageable.getOffset())
                          .paged(pageable.isPaged())
                          .unpaged(pageable.isUnpaged())
                          .build();
    }

}
