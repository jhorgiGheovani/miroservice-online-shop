package com.jhorgi.payment_service.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class CreatePaymentRequest {
    private UUID orderId;
    private String customerId;
    private BigDecimal amount;
    private String email;

    public UUID getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getEmail() { return email; }
}
