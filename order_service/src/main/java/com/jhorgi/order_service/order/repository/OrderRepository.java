package com.jhorgi.order_service.order.repository;

import com.jhorgi.order_service.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
