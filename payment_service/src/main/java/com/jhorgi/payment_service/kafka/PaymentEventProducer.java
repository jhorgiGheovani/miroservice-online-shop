package com.jhorgi.payment_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic.payment-events}")
    private String topic;

    public PaymentEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(UUID orderId, String paymentStatus, String email, BigDecimal amount) {
        String payload = String.format(
            "{\"orderId\":\"%s\",\"paymentStatus\":\"%s\",\"email\":\"%s\",\"amount\":%s}",
            orderId, paymentStatus, email, amount
        );
        kafkaTemplate.send(topic, payload);
        log.info("Published payment event: orderId={} status={}", orderId, paymentStatus);
    }
}
