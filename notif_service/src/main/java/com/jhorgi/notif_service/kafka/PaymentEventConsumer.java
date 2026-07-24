package com.jhorgi.notif_service.kafka;


import com.jhorgi.notif_service.email.EmailService;
import com.jhorgi.notif_service.kafka.dto.PaymentEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${app.kafka.topic.payment-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) throws Exception {
        PaymentEventDto event = objectMapper.readValue(message, PaymentEventDto.class);
        log.info("Received payment event: orderId={} status={}", event.getOrderId(), event.getPaymentStatus());
        emailService.sendPaymentNotification(
            event.getEmail(),
            event.getOrderId(),
            event.getPaymentStatus(),
            event.getAmount()
        );
    }
}
