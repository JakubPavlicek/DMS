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
            throw new FileOperationException(FileOperation.WRITE, "An error occurred while writing data from file: '" + filename + "' to storage");
        }

        log.info("Blob of the file {} stored successfully", filename);

        return hash;
    }

    public Resource getBlob(String hash) {
        Path filePath = getFilePath(hash);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            log.info("Blob retrieved succeffully");
            return resource;
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.READ, "An error occurred while reading the file");
        }
    }

    public void deleteBlob(String hash) {
        Path filePath = getFilePath(hash);

        try {
            boolean wasFileDeleted = Files.deleteIfExists(filePath);

            if (wasFileDeleted) {
                log.info("Blob deleted successfully");
            }
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.DELETE, "An error occurred while deleting the file");
        }

        Path directoryPath = getDirectoryPath(hash);

        if (isDirectoryEmpty(directoryPath)) {
            try {
                Files.deleteIfExists(directoryPath);
            } catch (Exception e) {
                throw new FileOperationException(FileOperation.DEFAULT, "An error occurred while working with the file");
            }
        }
    }

    private Path getDirectoryPath(String hash) {
        try {
            String directoryName = hash.substring(0, blobStorageProperties.getDirectoryPrefixLength());
            return Paths.get(blobStorageProperties.getPath(), directoryName);
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.DEFAULT, "An error occurred while working with the file");
        }
    }

    private Path getFilePath(String hash) {
        Path directoryPath = getDirectoryPath(hash);

        try {
            String directoryName = directoryPath.getName(directoryPath.getNameCount() - 1).toString();
            String fileName = hash.substring(blobStorageProperties.getDirectoryPrefixLength());
            return Paths.get(blobStorageProperties.getPath(), directoryName, fileName);
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.DEFAULT, "An error occurred while working with the file");
        }
    }

    private boolean isDirectoryEmpty(Path directoryPath) {
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(directoryPath)) {
            if (!Files.isDirectory(directoryPath)) {
                return false;
            }
            return !directory.iterator().hasNext();
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.DEFAULT, "An error occurred while working with the file");
        }
    }

}
