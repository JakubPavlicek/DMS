package com.dms.service;

import com.dms.config.BlobStorageProperties;
import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import com.dms.hash.Hasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class BlobStorageService {

    private final BlobStorageProperties blobStorageProperties;
    private final Hasher hasher;

    public String storeBlob(MultipartFile file) {
        String hash = hasher.hashFile(file);

        Path directoryPath = getDirectoryPath(hash);
        Path filePath = getFilePath(hash);

        try {
            if (Files.exists(filePath))
                return hash;

            if (Files.notExists(directoryPath))
                Files.createDirectory(directoryPath);

            Files.write(filePath, file.getBytes());
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.WRITE, "An error occurred while writing data from file: " + file.getOriginalFilename() + " to storage");
        }

        return hash;
    }

    public byte[] getBlob(String hash) {
        Path filePath = getFilePath(hash);

        try {
            return Files.readAllBytes(filePath);
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.READ, "An error occurred while reading the file");
        }
    }

    public void deleteBlob(String hash) {
        Path filePath = getFilePath(hash);

        try {
            Files.deleteIfExists(filePath);
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
            if (!Files.isDirectory(directoryPath))
                return false;
            return !directory.iterator().hasNext();
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.DEFAULT, "An error occurred while working with the file");
        }
    }

}
