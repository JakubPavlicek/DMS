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

/**
 * Service class for generating hash values for files.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class HashService {

    /** Configuration properties for hashing algorithm. */
    private final HashProperties hashProperties;

    /**
     * Generates a hash value for the provided file using the configured hashing algorithm.
     * @param file the file to be hashed
     * @return the hash value of the file
     * @throws FileOperationException if an error occurs while hashing the file
     */
    public String hashFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        log.info("Hashing file {}", filename);

        try {
            MessageDigest digest = MessageDigest.getInstance(hashProperties.getAlgorithm());
            byte[] hash = digest.digest(file.getBytes());
            return HexUtils.toHexString(hash);
        } catch (Exception e) {
            log.error("Failed to hash file {}", filename, e);
            throw new FileOperationException(FileOperation.READ);
        }
    }

}
