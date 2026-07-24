package com.jhorgi.notif_service.kafka.dto;

import java.math.BigDecimal;

public class PaymentEventDto {
    private String orderId;
    private String paymentStatus;
    private String email;
    private BigDecimal amount;

    public String getOrderId() { return orderId; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getEmail() { return email; }
    public BigDecimal getAmount() { return amount; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setEmail(String email) { this.email = email; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
