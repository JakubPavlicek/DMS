package com.dms.hash;

import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.Base64;

@Component
public class Hasher {

    @Value("${hash.algorithm}")
    private String hashAlgorithm;

    public String hashFile(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
            byte[] hash = digest.digest(file.getBytes());
            return Base64.getUrlEncoder()
                         .encodeToString(hash);
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.READ, "Nepodarilo se ziskat data souboru: " + file.getName());
        }
    }

}
