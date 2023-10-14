package com.dms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class FilterItem {

    private final String separator = ":";

    @NotBlank(message = "A filtering item must be provided")
    @Pattern(
        regexp = "^[a-zA-Z]+:.+$",
        message = "The format of filtering item does not match, the correct format is: <item>:<anything>"
    )
    private String fieldWithValue;

    public String getField() {
        return fieldWithValue.split(separator)[0];
    }

    public String getValue() {
        return fieldWithValue.split(separator)[1];
    }

}
