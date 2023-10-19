package com.dms.hash;

import com.dms.config.HashProperties;
import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;

@Component
@RequiredArgsConstructor
public class Hasher {

    private final HashProperties hashProperties;

    public String hashFile(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance(hashProperties.getAlgorithm());
            byte[] hash = digest.digest(file.getBytes());

            return HexUtils.toHexString(hash);
        } catch (Exception e) {
            throw new FileOperationException(FileOperation.READ, "Nepodarilo se ziskat data souboru: " + file.getName());
        }
    }

}
