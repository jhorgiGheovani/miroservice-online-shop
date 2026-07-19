package com.jhorgi.order_service.order.dto;

import java.math.BigDecimal;

public class OrderItemRequest {

    private String productId;
    private int quantity;
    private BigDecimal unitPrice;

    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
