package com.dms.util;

import lombok.extern.log4j.Log4j2;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

@Log4j2
public class KeyGenerator {

    public static KeyPair generateRsaKeys() {
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
