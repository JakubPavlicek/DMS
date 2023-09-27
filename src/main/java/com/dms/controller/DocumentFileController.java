package com.dms.controller;

import com.dms.entity.DocumentFile;
import com.dms.service.DocumentFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/document-files")
public class DocumentFileController {

    private final DocumentFileService documentFileService;

    @Autowired
    public DocumentFileController(DocumentFileService documentFileService) {
        this.documentFileService = documentFileService;
    }

    @PostMapping("/upload")
    public DocumentFile saveDocumentFile(@RequestBody MultipartFile file) {
        return documentFileService.saveFile(file);
    }

    @GetMapping("/download/{id}")
    public DocumentFile downloadDocumentFile(@PathVariable("id") String id)
    {
        return documentFileService.getDocumentFile(id);
    }
}
