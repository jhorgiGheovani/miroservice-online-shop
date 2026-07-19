package com.jhorgi.payment_service.payment.dto;

import com.jhorgi.payment_service.payment.entity.Payment;
import com.jhorgi.payment_service.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentResponse {
    private UUID paymentId;
    private UUID orderId;
    private String customerId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        PaymentResponse r = new PaymentResponse();
        r.paymentId = payment.getId();
        r.orderId = payment.getOrderId();
        r.customerId = payment.getCustomerId();
        r.amount = payment.getAmount();
        r.status = payment.getStatus();
        r.createdAt = payment.getCreatedAt();
        return r;
    }

    public UUID getPaymentId() { return paymentId; }
    public UUID getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
