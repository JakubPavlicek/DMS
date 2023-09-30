package com.dms.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentFileRequest {
    @NotNull(message = "File is mandatory")
    MultipartFile file;

    @NotBlank(message = "Author is mandatory")
    String author;
}
