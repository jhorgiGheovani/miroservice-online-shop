package com.jhorgi.payment_service.kafka;

import java.util.UUID;

public class PaymentEventDto {
    private UUID orderId;
    private String paymentStatus;

    public PaymentEventDto(UUID orderId, String paymentStatus) {
        this.orderId = orderId;
        this.paymentStatus = paymentStatus;
    }

    public UUID getOrderId() { return orderId; }
    public String getPaymentStatus() { return paymentStatus; }
}
