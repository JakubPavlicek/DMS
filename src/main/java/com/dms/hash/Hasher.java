package com.dms.hash;

import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import com.dms.exception.HashAlgorithmNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        } catch (NoSuchAlgorithmException e) {
            throw new HashAlgorithmNotFoundException("Hashovaci algoritmus: " + hashAlgorithm + " nebyl nalezen ci neexistuje");
        } catch (IOException e) {
            throw new FileOperationException(FileOperation.READ, "Nepodarilo se ziskat data souboru: " + file.getName());
        }
    }

}
