package com.dms.service;

import com.dms.config.HashProperties;
import com.dms.exception.FileOperation;
import com.dms.exception.FileOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;

@Service
@Log4j2
@RequiredArgsConstructor
public class HashService {

    private final HashProperties hashProperties;

    public String hashFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        log.info("Hashing file {}", filename);

        try {
            MessageDigest digest = MessageDigest.getInstance(hashProperties.getAlgorithm());
            byte[] hash = digest.digest(file.getBytes());

            return HexUtils.toHexString(hash);
        } catch (Exception e) {
            String message = "Failed to retrieve data from file: " + filename;
            log.error(message, e);
            throw new FileOperationException(FileOperation.READ, message);
        }
    }

}
