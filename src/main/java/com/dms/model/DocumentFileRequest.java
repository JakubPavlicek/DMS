package com.dms.model;

import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentFileRequest {
    MultipartFile file;
    String author;
}
