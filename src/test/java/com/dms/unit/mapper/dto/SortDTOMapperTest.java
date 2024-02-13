package com.dms.unit.mapper.dto;

import com.dms.dto.SortDTO;
import com.dms.mapper.dto.SortDTOMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.*;

class SortDTOMapperTest {

    @Test
    void whenValidSort_thenReturnSortDTO() {
        Sort sort = Sort.unsorted();

        SortDTO sortDTO = SortDTOMapper.map(sort);

        assertThat(sortDTO.getEmpty()).isEqualTo(sort.isEmpty());
        assertThat(sortDTO.getSorted()).isEqualTo(sort.isSorted());
        assertThat(sortDTO.getUnsorted()).isEqualTo(sort.isUnsorted());
    }

}