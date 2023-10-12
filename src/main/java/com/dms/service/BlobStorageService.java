package com.dms.service;

import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import com.dms.hash.Hasher;
import com.dms.storage.BlobStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class BlobStorageService {

    private final BlobStorage blobStorage;
    private final Hasher hasher;

    public String storeBlob(MultipartFile file) {
        String hash = hasher.hashFile(file);

        Path directoryPath = getDirectoryPath(hash);
        Path filePath = getFilePath(hash);

        if (Files.exists(filePath))
            return hash;

        try {
            if (Files.notExists(directoryPath))
                Files.createDirectory(directoryPath);

            Files.write(filePath, file.getBytes());
        } catch (IOException exception) {
            throw new FileOperationException(FileOperation.WRITE, "Zapis dat do souboru " + filePath + " s hashem: " + hash + " se nepodaril");
        }

        return hash;
    }

    public byte[] getBlob(String hash) {
        Path filePath = getFilePath(hash);

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new FileOperationException(FileOperation.READ, "Chyba pri nacitani souboru s hashem: " + hash);
        }
    }

    public void deleteBlob(String hash) {
        Path filePath = getFilePath(hash);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileOperationException(FileOperation.DELETE, "Chyba pri mazani souboru s hashem: " + hash);
        }

        Path directoryPath = getDirectoryPath(hash);

        if (isDirectoryEmpty(directoryPath)) {
            try {
                Files.deleteIfExists(directoryPath);
            } catch (IOException e) {
                throw new RuntimeException("Nepodarilo se smazat adresar: " + directoryPath);
            }
        }
    }

    private Path getDirectoryPath(String hash) {
        try {
            String directoryName = hash.substring(0, blobStorage.getDirectoryPrefixLength());
            return Paths.get(blobStorage.getStoragePath(), directoryName);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("Nepodarilo se vytvorit prefix delky: " + blobStorage.getDirectoryPrefixLength() + " pro adresar z hashe: " + hash);
        }
    }

    private Path getFilePath(String hash) {
        Path directoryPath = getDirectoryPath(hash);
        String directoryName = directoryPath.getName(directoryPath.getNameCount() - 1)
                                            .toString();

        try {
            String fileName = hash.substring(blobStorage.getDirectoryPrefixLength());
            return Paths.get(blobStorage.getStoragePath(), directoryName, fileName);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("Nepodarilo se ziskat nazev souboru z hashe: " + hash);
        }
    }

    private boolean isDirectoryEmpty(Path directoryPath) {
        if (!Files.isDirectory(directoryPath))
            return false;

        try (DirectoryStream<Path> directory = Files.newDirectoryStream(directoryPath)) {
            return !directory.iterator()
                             .hasNext();
        } catch (IOException e) {
            throw new RuntimeException("Nastala chyba pri praci s adresarem: " + directoryPath);
        }
    }

}
