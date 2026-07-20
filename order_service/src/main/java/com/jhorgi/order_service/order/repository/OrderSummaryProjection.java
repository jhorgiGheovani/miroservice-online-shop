package com.jhorgi.order_service.order.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderSummaryProjection {
    UUID getId();
    String getStatus();
    BigDecimal getGrandTotal();
    LocalDateTime getCreatedAt();
    long getItemCount();
    long getTotalQuantity();
}
