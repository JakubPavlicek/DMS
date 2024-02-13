package com.dms.util;

import com.dms.config.KeyProperties;
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
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Log4j2
public class KeyManager {

    private final static String ALGORITHM = "RSA";
    private final static int KEY_SIZE = 2048;

    private final static String PRIVATE_KEY = "RSA PRIVATE";
    private final static String PUBLIC_KEY = "RSA PUBLIC";

    private final KeyProperties keyProperties;

    private KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            log.info("Successfully generated key pair");
            return keyPair;
        } catch (Exception exception) {
            String message = "Key pair couldn't be created";
            log.error(message, exception);
            throw new RuntimeException(message);
        }
    }

    public RSAKey getRsaKey() {
        Path privateKeyPath = Paths.get(keyProperties.getPrivateKey());
        Path publicKeyPath = Paths.get(keyProperties.getPublicKey());

        RSAPrivateKey privateKey;
        RSAPublicKey publicKey;

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

        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        log.info("Successfully created {} key", ALGORITHM);
        return rsaKey;
    }

    private Key loadKey(String keyType, Path keyPath) {
        try {
            byte[] keyBytes = Files.readAllBytes(keyPath);

            String encodedKey = new String(keyBytes)
                .replaceAll("\n", "")
                .replace("-----BEGIN " + keyType + " KEY-----", "")
                .replace("-----END " + keyType + " KEY-----", "");

            keyBytes = Base64.getDecoder().decode(encodedKey);

            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

            if (keyType.equals(PRIVATE_KEY))
            {
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                return keyFactory.generatePrivate(keySpec);
            }
            else
            {
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                return keyFactory.generatePublic(keySpec);
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException exception) {
            String message = "Couldn't load " + keyType + " key";
            log.error(message, exception);
            throw new RuntimeException(message);
        }
    }

    private void writeKeyToFile(String keyType, Key key, String filepath) {
        byte[] keyBytes = key.getEncoded();
        String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

        try(FileWriter fileWriter = new FileWriter(filepath))
        {
            fileWriter.write("-----BEGIN " + keyType + " KEY-----\n");
            fileWriter.write(encodedKey);
            fileWriter.write("\n-----END " + keyType + " KEY-----");
        } catch (IOException exception) {
            String message = "Failed to save " + keyType + " key";
            log.error(message, exception);
            throw new RuntimeException(exception);
        }

        log.info("Successfully saved {} key to file {}", keyType, filepath);
    }

}
