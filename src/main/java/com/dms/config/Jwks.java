package com.dms.config;

import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class Jwks {

    public static RSAKey generateRsa() {
        KeyPair keyPair = KeyGenerator.generateRsaKeys();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        return new RSAKey.Builder(publicKey).privateKey(privateKey)
                                            .build();
    }

}
