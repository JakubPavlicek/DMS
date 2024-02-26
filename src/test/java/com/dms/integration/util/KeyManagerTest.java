package com.dms.integration.util;

import com.dms.config.KeyProperties;
import com.dms.util.KeyManager;
import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class KeyManagerTest {

    @Autowired
    private KeyProperties keyProperties;

    @Autowired
    private KeyManager keyManager;

    @BeforeEach
    void setUp() throws IOException {
        deleteKeys();
    }

    @AfterEach
    void tearDown() throws IOException {
        deleteKeys();
    }

    @Test
    void shouldGenerateRsaKeyPairWhenKeyDoesNotExist() throws KeyException {
        keyManager.getRsaKey();

        Path privateKeyPath = Path.of(keyProperties.getPrivateKey());
        Path publicKeyPath = Path.of(keyProperties.getPublicKey());

        boolean privateKeyExists = Files.exists(privateKeyPath);
        boolean publicKeyExists = Files.exists(publicKeyPath);

        assertThat(privateKeyExists).isTrue();
        assertThat(privateKeyPath.toFile()).isFile();
        assertThat(privateKeyPath.getFileName().toString()).endsWith(".pem");

        assertThat(publicKeyExists).isTrue();
        assertThat(publicKeyPath.toFile()).isFile();
        assertThat(publicKeyPath.getFileName().toString()).endsWith(".pem");
    }

    @Test
    void shouldLoadRsaKeyWhenKeyAlreadyExists() throws KeyException {
        RSAKey generatedRsaKey = keyManager.getRsaKey();
        RSAKey loadedRsaKey = keyManager.getRsaKey();

        assertThat(generatedRsaKey).isNotNull();
        assertThat(loadedRsaKey).isNotNull();
        assertThat(loadedRsaKey.getKeyID()).isEqualTo(generatedRsaKey.getKeyID());
    }

    private void deleteKeys() throws IOException {
        Path privateKeyPath = Path.of(keyProperties.getPrivateKey());
        Path publicKeyPath = Path.of(keyProperties.getPublicKey());

        Files.deleteIfExists(privateKeyPath);
        Files.deleteIfExists(publicKeyPath);
    }

}