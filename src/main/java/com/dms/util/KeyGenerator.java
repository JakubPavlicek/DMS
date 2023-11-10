package com.dms.util;

import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
@Log4j2
public class KeyGenerator {

    public RSAKey generateRsaKey() {
        KeyPair keyPair = generateRsaKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        log.info("Successfully generated RSA Key");
        return rsaKey;
    }

    private KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            log.info("Successfully generated key pair");
            return keyPair;
        } catch (Exception e) {
            throw new RuntimeException("RSA key pair couldn't be created");
        }
    }

}
