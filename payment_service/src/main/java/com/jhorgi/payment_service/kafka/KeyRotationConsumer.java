package com.jhorgi.payment_service.kafka;

import com.jhorgi.payment_service.config.RsaKeyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

@Component
public class KeyRotationConsumer {

    private static final Logger log = LoggerFactory.getLogger(KeyRotationConsumer.class);

    private final RsaKeyConfig rsaKeyConfig;

    public KeyRotationConsumer(RsaKeyConfig rsaKeyConfig) {
        this.rsaKeyConfig = rsaKeyConfig;
    }

    @KafkaListener(topics = "${app.kafka.topic.key-rotation}", groupId = "${spring.kafka.consumer.group-id}")
    public void onKeyRotation(String base64PublicKey) {
        log.info("Received rotated public key");
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        try {
            rsaKeyConfig.persistPublicKey(keyBytes);
            log.info("Public key rotated and persisted.");
        } catch (IOException e) {
            log.error("Failed to persist rotated public key", e);
        }
    }
}
