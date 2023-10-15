package com.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FilterItem {

    private String field;
    private String value;

}
