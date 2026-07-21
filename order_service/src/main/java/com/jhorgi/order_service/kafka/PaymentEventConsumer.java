package com.jhorgi.order_service.kafka;


import com.jhorgi.order_service.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(OrderRepository orderRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topic.payment-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            UUID orderId = UUID.fromString(node.get("orderId").asText());
            String paymentStatus = node.get("paymentStatus").asText();

            String newOrderStatus = switch (paymentStatus) {
                case "SUCCESS" -> "COMPLETED";
                case "FAILED"  -> "FAILED";
                default -> {
                    log.warn("Unknown paymentStatus '{}', skipping", paymentStatus);
                    yield null;
                }
            };

            if (newOrderStatus == null) return;

            orderRepository.findById(orderId).ifPresentOrElse(order -> {
                order.setStatus(newOrderStatus);
                orderRepository.save(order);
                log.info("Order {} status updated to {}", orderId, newOrderStatus);
            }, () -> log.warn("Order not found for orderId: {}", orderId));

        } catch (Exception e) {
            log.error("Failed to process payment event payload: {}", payload, e);
        }
    }
}
