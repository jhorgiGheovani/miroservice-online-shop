package com.jhorgi.order_service.order.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "grand_total", nullable = false)
    private BigDecimal grandTotal;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    public Order() {}

    public Order(String customerId, BigDecimal grandTotal, String status, LocalDateTime createdAt, List<OrderItem> items) {
        this.customerId = customerId;
        this.grandTotal = grandTotal;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
        this.items.forEach(item -> item.setOrder(this));
    }

    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}
