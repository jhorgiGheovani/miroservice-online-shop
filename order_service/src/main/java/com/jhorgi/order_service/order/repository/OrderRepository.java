package com.jhorgi.order_service.order.repository;

import com.jhorgi.order_service.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query(
        value = """
            SELECT
                o.id,
                o.status,
                o.grand_total   AS grandTotal,
                o.created_at    AS createdAt,
                COUNT(oi.id)    AS itemCount,
                SUM(oi.quantity) AS totalQuantity
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            WHERE o.customer_id = :customerId
            GROUP BY o.id, o.status, o.grand_total, o.created_at
            ORDER BY o.created_at DESC
            """,
        nativeQuery = true
    )
    List<OrderSummaryProjection> findOrderSummariesByCustomerId(@Param("customerId") String customerId);
}
