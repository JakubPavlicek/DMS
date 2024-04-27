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

/**
 * Service class for managing blob storage operations.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class BlobStorageService {

    /** Properties related to blob storage configuration. */
    private final BlobStorageProperties blobStorageProperties;
    /** Service for generating hash values for files. */
    private final HashService hashService;

    /**
     * Stores a blob in the file system.
     *
     * @param file the multipart file to store
     * @return the hash value of the stored blob
     * @throws FileOperationException if an error occurs during the storage process
     */
    public String storeBlob(MultipartFile file) {
        String hash = hashService.hashFile(file);
        String filename = file.getOriginalFilename();

        Path directoryPath = getDirectoryPath(hash);
        Path filePath = getFilePath(hash);

        try {
            // don't store blob if it already exists
            if (Files.exists(filePath)) {
                log.info("Blob of the file {} already exist, retrieving existing blob", filename);
                return hash;
            }

            // create directory if it doesn't exist
            if (Files.notExists(directoryPath)) {
                Files.createDirectory(directoryPath);
                log.info("Creating a directory for blob of the file {}", filename);
            }

            // copy file content to the blob file
            InputStream fileStream = file.getInputStream();
            Files.copy(fileStream, filePath);
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.WRITE);
        }

        log.info("Blob of the file {} stored successfully", filename);

        return hash;
    }

    /**
     * Retrieves a blob from the file system.
     *
     * @param hash the hash value of the blob to retrieve
     * @return the resource representing the retrieved blob
     * @throws FileOperationException if the blob retrieval fails
     */
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

    /**
     * Deletes a blob from the file system.
     *
     * @param hash the hash value of the blob to delete
     * @throws FileOperationException if the blob deletion fails
     */
    public void deleteBlob(String hash) {
        Path filePath = getFilePath(hash);

        try {
            // attempt to delete the blob file
            boolean wasFileDeleted = Files.deleteIfExists(filePath);

            // delete directory if it is empty after the deletion
            if (wasFileDeleted) {
                log.info("Blob deleted successfully");

                deleteDirectoryIfEmpty(hash);
            }
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.DELETE);
        }
    }

    /**
     * Deletes the directory if it is empty.
     *
     * @param hash the hash value used to identify the directory
     */
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

    /**
     * Retrieves the directory name from the hash.
     *
     * @param hash the hash value from which to extract the directory name
     * @return the directory name extracted from the hash
     */
    private String getDirectoryName(String hash) {
        return hash.substring(0, blobStorageProperties.getSubdirectoryPrefixLength());
    }

    /**
     * Retrieves the directory path corresponding to the hash.
     *
     * @param hash the hash value used to identify the directory
     * @return the path to the directory identified by the hash
     */
    private Path getDirectoryPath(String hash) {
        try {
            String directoryName = getDirectoryName(hash);
            return Paths.get(blobStorageProperties.getPath(), directoryName);
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.DEFAULT);
        }
    }

    /**
     * Retrieves the file path corresponding to the hash.
     *
     * @param hash the hash value used to identify the file
     * @return the path to the file identified by the hash
     */
    private Path getFilePath(String hash) {
        try {
            String directoryName = getDirectoryName(hash);
            String fileName = hash.substring(blobStorageProperties.getSubdirectoryPrefixLength());
            return Paths.get(blobStorageProperties.getPath(), directoryName, fileName);
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.DEFAULT);
        }
    }

    /**
     * Checks if the directory identified by the path is empty.
     *
     * @param directoryPath the path to the directory to check
     * @return true if the directory is empty, false otherwise
     */
    private boolean isDirectoryEmpty(Path directoryPath) {
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(directoryPath)) {
            // if the provided path is not a directory, it is not empty
            if (!Files.isDirectory(directoryPath)) {
                return false;
            }
            // check if the directory is empty using iterator's next element existence
            return !directory.iterator().hasNext();
        } catch (Exception exception) {
            throw new FileOperationException(FileOperation.DEFAULT);
        }
    }

}
