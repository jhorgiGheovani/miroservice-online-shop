package com.jhorgi.auth_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KeyRotationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;
    private static final Logger log = LoggerFactory.getLogger(KeyRotationProducer.class);

    public KeyRotationProducer(KafkaTemplate<String, String> kafkaTemplate,
                               @Value("${app.kafka.topic.key-rotation}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    // Message key "current" ensures log compaction keeps only the latest public key
    public void publishPublicKey(String base64PublicKey) {
        kafkaTemplate.send(topic, "current", base64PublicKey).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish public key to topic {}: {}", topic, ex.getMessage());
            } else {
                log.info("Public key published to topic {} partition {} offset {}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
