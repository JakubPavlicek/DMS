package com.dms.service;

import com.dms.dto.DocumentDTO;
import com.dms.dto.DocumentRevisionDTO;
import com.dms.dto.UserDTO;
import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.User;
import com.dms.exception.DocumentNotFoundException;
import com.dms.exception.RevisionNotFoundException;
import com.dms.repository.DocumentRepository;
import com.dms.repository.DocumentRevisionRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class DocumentCommonService {

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository revisionRepository;

    private final BlobStorageService blobStorageService;
    private final ModelMapper modelMapper;

    public Document getDocument(String documentId) {
        return documentRepository.findById(documentId)
                                 .orElseThrow(() -> new DocumentNotFoundException("Soubor s id: " + documentId + " nebyl nalezen."));
    }

    public DocumentRevision getRevision(Long revisionId) {
        return revisionRepository.findByRevisionId(revisionId)
                                 .orElseThrow(() -> new RevisionNotFoundException("Revize s ID: " + revisionId + " nebyla nalezena"));
    }

    public void updateDocumentToRevision(Document document, DocumentRevision documentRevision) {
        document.setName(documentRevision.getName());
        document.setType(documentRevision.getType());
        document.setAuthor(documentRevision.getAuthor());
        document.setHash(documentRevision.getHash());

        documentRepository.save(document);
    }

    public void createDocumentRevision(Document document) {
        DocumentRevision documentRevision = DocumentRevision.builder()
                                                            .document(document)
                                                            .version(getLastRevisionVersion(document) + 1)
                                                            .name(document.getName())
                                                            .type(document.getType())
                                                            .author(document.getAuthor())
                                                            .hash(document.getHash())
                                                            .build();
        revisionRepository.save(documentRevision);
    }

    private Long getLastRevisionVersion(Document document) {
        return revisionRepository.findLastRevisionVersionByDocument(document)
                                 .orElse(0L);
    }

    public String storeBlob(MultipartFile file) {
        return blobStorageService.storeBlob(file);
    }

    public byte[] getBlob(String hash) {
        return blobStorageService.getBlob(hash);
    }

    public void deleteBlobIfDuplicateHashNotExists(String hash) {
        if (!isDuplicateHashPresent(hash))
            blobStorageService.deleteBlob(hash);
    }

    private boolean isDuplicateHashPresent(String hash) {
        return documentRepository.duplicateHashExists(hash) || revisionRepository.duplicateHashExists(hash);
    }

    public DocumentDTO mapDocumentToDocumentDto(Document document) {
        return modelMapper.map(document, DocumentDTO.class);
    }

    public DocumentRevisionDTO mapRevisionToRevisionDto(DocumentRevision revision) {
        return modelMapper.map(revision, DocumentRevisionDTO.class);
    }

    public User mapUserDtoToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

}
