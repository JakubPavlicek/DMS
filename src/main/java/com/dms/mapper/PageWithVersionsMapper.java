package com.dms.mapper;

import com.dms.dto.PageWithVersions;
import org.springframework.data.domain.Page;

public class PageWithVersionsMapper {

    public static PageWithVersions map(Page<Long> versionsPage) {
        return PageWithVersions.builder()
                               .content(versionsPage.getContent())
                               .pageable(PageableDTOMapper.map(versionsPage.getPageable()))
                               .last(versionsPage.isLast())
                               .totalElements(versionsPage.getTotalElements())
                               .totalPages(versionsPage.getTotalPages())
                               .first(versionsPage.isFirst())
                               .size(versionsPage.getSize())
                               .number(versionsPage.getNumber())
                               .sort(SortDTOMapper.map(versionsPage.getSort()))
                               .numberOfElements(versionsPage.getNumberOfElements())
                               .empty(versionsPage.isEmpty())
                               .build();
    }

}
