package com.dms.util;

import com.dms.config.KeyProperties;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

@Component
@RequiredArgsConstructor
@Log4j2
public class KeyManager {

    private final static String ALGORITHM = "RSA";

    private final KeyProperties keyProperties;

    public RSAKey getRsaKey() {
        Path privateKeyPath = Paths.get(keyProperties.getPrivateKey());
        Path publicKeyPath = Paths.get(keyProperties.getPublicKey());

        RSAPublicKey publicKey;
        RSAPrivateKey privateKey;

        if(Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            privateKey = loadPrivateKey(privateKeyPath);
            publicKey = loadPublicKey(publicKeyPath);
        }
        else {
            KeyPair keyPair = generateRsaKeyPair();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
            publicKey = (RSAPublicKey) keyPair.getPublic();

            writeKeyToFile("RSA PRIVATE KEY", privateKey, privateKeyPath.toString());
            writeKeyToFile("RSA PUBLIC KEY", publicKey, publicKeyPath.toString());
        }

        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        log.info("Successfully generated RSA Key");
        return rsaKey;
    }

    private RSAPrivateKey loadPrivateKey(Path privateKeyPath) {
        try(FileReader fileReader = new FileReader(privateKeyPath.toString())) {
            PemReader pemReader = new PemReader(fileReader);
            PemObject pemObject = pemReader.readPemObject();
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
            return (RSAPrivateKey) KeyFactory.getInstance(ALGORITHM).generatePrivate(privateKeySpec);
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private RSAPublicKey loadPublicKey(Path publicKeyPath) {
        try(FileReader fileReader = new FileReader(publicKeyPath.toString())){
            PemReader pemReader = new PemReader(fileReader);
            PemObject pemObject = pemReader.readPemObject();
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pemObject.getContent());
            return (RSAPublicKey) KeyFactory.getInstance(ALGORITHM).generatePublic(publicKeySpec);
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            log.info("Successfully generated key pair");
            return keyPair;
        } catch (Exception e) {
            String message = "RSA key pair couldn't be created";
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    private void writeKeyToFile(String description, Key key, String filepath) {
        PemObject pemKey = new PemObject(description, key.getEncoded());

        try(PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(filepath)))) {
            pemWriter.writeObject(pemKey);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save key: " + description);
        }

        log.info("Successfully saved key {} to file {}", description, filepath);
    }

}
