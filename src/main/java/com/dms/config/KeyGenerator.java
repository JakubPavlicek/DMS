package com.dms.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class KeyGenerator {

    public static KeyPair generateRsaKeys() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("RSA keys couldn't be created");
        }
    }

}
