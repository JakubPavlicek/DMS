package com.dms.service;

import com.dms.exception.FileAlreadyExistsException;
import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import com.dms.hash.Sha256Hasher;
import com.dms.storage.BlobStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class BlobStorageService {

    private final BlobStorage blobStorage;
    private final Sha256Hasher hasher;

    @Autowired
    public BlobStorageService(BlobStorage blobStorage, Sha256Hasher hasher) {
        this.blobStorage = blobStorage;
        this.hasher = hasher;
    }

    public String storeBlob(MultipartFile file) {
        String hash = hasher.hashFile(file);
        Path filePath = Paths.get(blobStorage.getStoragePath(), hash);

        if (Files.exists(filePath))
            throw new FileAlreadyExistsException("Soubor " + file.getOriginalFilename() + " s hashem " + hash + " jiz existuje");

        try {
            Files.write(filePath, file.getBytes());
        } catch (IOException exception) {
            throw new FileOperationException(FileOperation.WRITE, "Zapis dat do souboru " + filePath + " s hashem: " + hash + " se nepodaril");
        }

        return hash;
    }

    public byte[] getBlob(String hash) {
        String filePath = Paths.get(blobStorage.getStoragePath(), hash)
                               .toString();
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new FileOperationException(FileOperation.READ, "Chyba pri nacitani souboru s hashem: " + hash);
        }
    }

    public void deleteBlob(String hash) {
        String filePath = Paths.get(blobStorage.getStoragePath(), hash)
                               .toString();
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new FileOperationException(FileOperation.DELETE, "Chyba pri mazani souboru s hashem: " + hash);
        }
    }

}
