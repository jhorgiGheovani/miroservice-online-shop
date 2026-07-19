package com.jhorgi.auth_service.service;

import com.jhorgi.auth_service.config.RsaKeyConfig;
import com.jhorgi.auth_service.kafka.KeyRotationProducer;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

@Service
public class KeyRotationService {

    private final RsaKeyConfig rsaKeyConfig;
    private final KeyRotationProducer producer;

    public KeyRotationService(RsaKeyConfig rsaKeyConfig, KeyRotationProducer producer) {
        this.rsaKeyConfig = rsaKeyConfig;
        this.producer = producer;
    }

    public void rotateKeys() throws Exception {
        KeyPair newKeyPair = generateRsaKeyPair();
        rsaKeyConfig.updateKeyPair(newKeyPair);
        rsaKeyConfig.persistKeyPair(newKeyPair); //catat di file pem

        String base64PublicKey = Base64.getEncoder()
                .encodeToString(newKeyPair.getPublic().getEncoded());
        producer.publishPublicKey(base64PublicKey);
    }

    private KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }
}
