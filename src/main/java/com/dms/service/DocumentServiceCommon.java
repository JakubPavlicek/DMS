package com.dms.service;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.exception.DocumentNotFoundException;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DocumentServiceCommon {

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository revisionRepository;

    public Document getDocument(String documentId) {
        return documentRepository.findById(documentId)
                                 .orElseThrow(() -> new DocumentNotFoundException("Soubor s id: " + documentId + " nebyl nalezen."));
    }

    public void updateDocumentToRevision(Document document, DocumentRevision documentRevision) {
        document.setName(documentRevision.getName());
        document.setExtension(documentRevision.getExtension());
        document.setType(documentRevision.getType());
        document.setAuthor(documentRevision.getAuthor());
        document.setHashPointer(documentRevision.getHashPointer());

        documentRepository.save(document);
    }

    public void createDocumentRevision(Document document) {
        DocumentRevision documentRevision = DocumentRevision.builder()
                                                            .document(document)
                                                            .version(getLastRevisionVersion(document) + 1)
                                                            .name(document.getName())
                                                            .extension(document.getExtension())
                                                            .type(document.getType())
                                                            .author(document.getAuthor())
                                                            .hashPointer(document.getHashPointer())
                                                            .build();
        revisionRepository.save(documentRevision);
    }

    private Long getLastRevisionVersion(Document document) {
        return revisionRepository.findLastRevisionVersionByDocument(document)
                                 .orElse(0L);
    }

}
