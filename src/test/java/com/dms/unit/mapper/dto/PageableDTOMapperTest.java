package com.dms.unit.mapper.dto;

import com.dms.dto.PageableDTO;
import com.dms.mapper.dto.PageableDTOMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.*;

class PageableDTOMapperTest {

    @Test
    void whenValidPageable_thenReturnPageableDTO() {
        Pageable pageable = Pageable.ofSize(10);

        PageableDTO pageableDTO = PageableDTOMapper.map(pageable);

        assertThat(pageableDTO.getPageNumber()).isEqualTo(pageable.getPageNumber());
        assertThat(pageableDTO.getPageSize()).isEqualTo(pageable.getPageSize());
        assertThat(pageableDTO.getSort()).isNotNull();
        assertThat(pageableDTO.getOffset()).isEqualTo(pageable.getOffset());
        assertThat(pageableDTO.getPaged()).isEqualTo(pageable.isPaged());
        assertThat(pageableDTO.getUnpaged()).isEqualTo(pageable.isUnpaged());
    }

}