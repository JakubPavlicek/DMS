package com.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
@AllArgsConstructor
public class SortItem {

    private String field;
    private Sort.Direction direction;

}
