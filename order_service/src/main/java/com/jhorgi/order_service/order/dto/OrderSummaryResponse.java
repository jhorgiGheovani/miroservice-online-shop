package com.jhorgi.order_service.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderSummaryResponse {

    private UUID id;
    private String status;
    private BigDecimal grandTotal;
    private LocalDateTime createdAt;
    private long itemCount;
    private long totalQuantity;

    public OrderSummaryResponse(UUID id, String status, BigDecimal grandTotal,
                                LocalDateTime createdAt, long itemCount, long totalQuantity) {
        this.id = id;
        this.status = status;
        this.grandTotal = grandTotal;
        this.createdAt = createdAt;
        this.itemCount = itemCount;
        this.totalQuantity = totalQuantity;
    }

    public UUID getId() { return id; }
    public String getStatus() { return status; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public long getItemCount() { return itemCount; }
    public long getTotalQuantity() { return totalQuantity; }
}
