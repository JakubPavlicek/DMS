package com.dms.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentDestinationRequest {
    @NotBlank(message = "Destination path is mandatory")
    private String destination;
}
