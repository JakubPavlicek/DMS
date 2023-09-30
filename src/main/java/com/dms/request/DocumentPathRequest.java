package com.dms.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentPathRequest {
    @NotBlank(message = "Path is mandatory")
    private String path;
}
