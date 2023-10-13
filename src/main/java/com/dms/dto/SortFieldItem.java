package com.dms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class SortFieldItem {

    private final String separator = ":";

    @NotBlank(message = "A sorting item must be provided")
    @Pattern(
        regexp = "^[a-zA-Z]+:(asc|desc)$",
        message = "The format of sorting items does not match, the correct format is: <item>:<asc/desc>"
    )
    private String fieldWithOrder;

    public String getField() {
        return fieldWithOrder.split(separator)[0];
    }

    public String getOrder() {
        return fieldWithOrder.split(separator)[1];
    }

}
