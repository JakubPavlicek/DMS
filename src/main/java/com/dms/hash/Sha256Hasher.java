package com.dms.hash;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class Sha256Hasher {
    public String hashFile(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return Base64.getUrlEncoder()
                         .encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashovaci algoritmus neexistuje");
        } catch (IOException e) {
            throw new RuntimeException("Nepodarilo se ziskat data souboru: " + file.getName());
        }
    }
}
