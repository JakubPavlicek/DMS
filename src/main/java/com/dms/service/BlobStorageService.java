package com.dms.service;

import com.dms.config.BlobStorageProperties;
import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Log4j2
@RequiredArgsConstructor
public class BlobStorageService {

    private final BlobStorageProperties blobStorageProperties;
    private final HashService hashService;

    public String storeBlob(MultipartFile file) {
        String hash = hashService.hashFile(file);
        String filename = file.getOriginalFilename();

        Path directoryPath = getDirectoryPath(hash);
        Path filePath = getFilePath(hash);

        try {
            if (Files.exists(filePath)) {
                log.info("Blob of the file {} already exist, retrieving existing blob", filename);
                return hash;
            }

            if (Files.notExists(directoryPath)) {
                Files.createDirectory(directoryPath);
                log.info("Creating a directory for blob of the file {}", filename);
            }

            InputStream fileStream = file.getInputStream();
            Files.copy(fileStream, filePath);
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.WRITE);
        }

        log.info("Blob of the file {} stored successfully", filename);

        return hash;
    }

    public Resource getBlob(String hash) {
        Path filePath = getFilePath(hash);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            log.info("Blob retrieved successfully");
            return resource;
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.READ);
        }
    }

    public void deleteBlob(String hash) {
        Path filePath = getFilePath(hash);

        try {
            boolean wasFileDeleted = Files.deleteIfExists(filePath);

            if (wasFileDeleted) {
                log.info("Blob deleted successfully");

                deleteDirectoryIfEmpty(hash);
            }
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.DELETE);
        }
    }

    private void deleteDirectoryIfEmpty(String hash) {
        Path directoryPath = getDirectoryPath(hash);

        if (isDirectoryEmpty(directoryPath)) {
            try {
                Files.deleteIfExists(directoryPath);
            } catch (Exception exception) {
                throw new FileOperationException(FileOperation.DEFAULT);
            }
        }
    }

    private String getDirectoryName(String hash) {
        return hash.substring(0, blobStorageProperties.getDirectoryPrefixLength());
    }

    private Path getDirectoryPath(String hash) {
        try {
            String directoryName = getDirectoryName(hash);
            return Paths.get(blobStorageProperties.getPath(), directoryName);
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.DEFAULT);
        }
    }

    private Path getFilePath(String hash) {
        try {
            String directoryName = getDirectoryName(hash);
            String fileName = hash.substring(blobStorageProperties.getDirectoryPrefixLength());
            return Paths.get(blobStorageProperties.getPath(), directoryName, fileName);
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.DEFAULT);
        }
    }

    private boolean isDirectoryEmpty(Path directoryPath) {
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(directoryPath)) {
            if (!Files.isDirectory(directoryPath)) {
                return false;
            }
            return !directory.iterator().hasNext();
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.DEFAULT);
        }
    }

}
