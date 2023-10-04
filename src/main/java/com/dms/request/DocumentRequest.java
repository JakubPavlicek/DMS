package com.dms.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class DocumentRequest {
    @NotNull(message = "File is mandatory")
    private MultipartFile file;

    @NotBlank(message = "User is mandatory")
    private String user;
}
