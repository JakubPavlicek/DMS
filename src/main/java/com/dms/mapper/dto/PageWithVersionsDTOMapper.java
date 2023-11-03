package com.dms.mapper.dto;

import com.dms.dto.PageWithVersionsDTO;
import org.springframework.data.domain.Page;

public class PageWithVersionsDTOMapper {

    public static PageWithVersionsDTO map(Page<Long> versionsPage) {
        return PageWithVersionsDTO.builder()
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
