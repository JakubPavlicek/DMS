package com.dms.util;

import com.dms.config.KeyProperties;
import com.dms.exception.KeyException;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * The {@code KeyManager} class provides utility methods for managing RSA keys.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class KeyManager {

    /** The algorithm used for RSA keys. */
    private static final String ALGORITHM = "RSA";
    /** The size of the RSA key. */
    private static final int KEY_SIZE = 2048;

    /** Key type for private key. */
    private static final String PRIVATE_KEY = "RSA PRIVATE";
    /** Key type for public key. */
    private static final String PUBLIC_KEY = "RSA PUBLIC";

    /** Key begin tag. */
    private static final String KEY_BEGIN = "-----BEGIN ";
    /** Key end tag. */
    private static final String KEY_END = "-----END ";
    /** Key suffix. */
    private static final String KEY_SUFFIX = " KEY-----";

    /** The properties for key configuration. */
    private final KeyProperties keyProperties;

    /**
     * Generates an RSA key pair.
     *
     * @return the generated RSA key pair
     * @throws KeyException if the key pair generation fails
     */
    private KeyPair generateRsaKeyPair() throws KeyException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            log.info("Successfully generated key pair");
            return keyPair;
        } catch (Exception exception) {
            String message = "Key pair couldn't be created";
            log.error(message, exception);
            throw new KeyException(message);
        }
    }

    /**
     * Retrieves the RSA key.
     *
     * @return the RSA key
     * @throws KeyException if the RSA key retrieval fails
     */
    public RSAKey getRsaKey() throws KeyException {
        Path privateKeyPath = Paths.get(keyProperties.getPrivateKey());
        Path publicKeyPath = Paths.get(keyProperties.getPublicKey());

        RSAPrivateKey privateKey;
        RSAPublicKey publicKey;

        // if keys exist in files, load them; otherwise, generate new keys
        if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            privateKey = (RSAPrivateKey) loadKey(PRIVATE_KEY, privateKeyPath);
            publicKey = (RSAPublicKey) loadKey(PUBLIC_KEY, publicKeyPath);
        }
        else {
            KeyPair keyPair = generateRsaKeyPair();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
            publicKey = (RSAPublicKey) keyPair.getPublic();

            writeKeyToFile(PRIVATE_KEY, privateKey, privateKeyPath.toString());
            writeKeyToFile(PUBLIC_KEY, publicKey, publicKeyPath.toString());
        }

        // build RSA key using public and private keys
        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();

        log.info("Successfully created {} key", ALGORITHM);
        return rsaKey;
    }

    /**
     * Loads a key from a file.
     *
     * @param keyType the type of the key (private or public)
     * @param keyPath the path to the key file
     * @return the loaded key
     * @throws KeyException if loading the key fails
     */
    private Key loadKey(String keyType, Path keyPath) throws KeyException {
        try {
            byte[] keyBytes = Files.readAllBytes(keyPath);

            // remove unnecessary characters from key string
            String encodedKey = new String(keyBytes)
                .replace("\n", "")
                .replace(KEY_BEGIN + keyType + KEY_SUFFIX, "")
                .replace(KEY_END + keyType + KEY_SUFFIX, "");

            keyBytes = Base64.getDecoder().decode(encodedKey);

            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

            // if key type is private key, parse it; otherwise parse public key
            if (keyType.equals(PRIVATE_KEY)) {
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                return keyFactory.generatePrivate(keySpec);
            }
            else {
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                return keyFactory.generatePublic(keySpec);
            }
        } catch (Exception exception) {
            String message = "Couldn't load " + keyType + " key";
            log.error(message, exception);
            throw new KeyException(message);
        }
    }

    /**
     * Writes a key to a file.
     *
     * @param keyType the type of the key (private or public)
     * @param key the key to write
     * @param filepath the file path to write the key to
     * @throws KeyException if writing the key fails
     */
    private void writeKeyToFile(String keyType, Key key, String filepath) throws KeyException {
        byte[] keyBytes = key.getEncoded();
        String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

        // write key to file with proper formatting
        try (FileWriter fileWriter = new FileWriter(filepath)) {
            fileWriter.write(KEY_BEGIN + keyType + KEY_SUFFIX);
            fileWriter.write("\n");
            fileWriter.write(encodedKey);
            fileWriter.write("\n");
            fileWriter.write(KEY_END + keyType + KEY_SUFFIX);
        } catch (IOException exception) {
            String message = "Failed to save " + keyType + " key";
            log.error(message, exception);
            throw new KeyException(message);
        }

        log.info("Successfully saved {} key to file {}", keyType, filepath);
    }

}
