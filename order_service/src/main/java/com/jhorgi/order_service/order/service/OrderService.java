package com.jhorgi.order_service.order.service;

import com.jhorgi.order_service.order.dto.CreateOrderRequest;
import com.jhorgi.order_service.order.dto.OrderSummaryResponse;
import com.jhorgi.order_service.order.entity.Order;
import com.jhorgi.order_service.order.entity.OrderItem;
import com.jhorgi.order_service.order.repository.OrderRepository;
import com.jhorgi.order_service.payment.PaymentClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;

    public OrderService(OrderRepository orderRepository, PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
    }

    public Order createOrder(CreateOrderRequest request, String customerId, String email) {
        List<OrderItem> items = request.getItems().stream()
                .map(i -> new OrderItem(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        BigDecimal grandTotal = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(customerId, grandTotal, "PENDING", LocalDateTime.now(), items);
        Order saved = orderRepository.save(order);

        paymentClient.createPayment(saved.getId(), saved.getGrandTotal(), customerId, email);

        return saved;
    }

    public List<OrderSummaryResponse> getMyOrders(String customerId) {
        return orderRepository.findOrderSummariesByCustomerId(customerId)
                .stream()
                .map(p -> new OrderSummaryResponse(
                        p.getId(), p.getStatus(), p.getGrandTotal(),
                        p.getCreatedAt(), p.getItemCount(), p.getTotalQuantity()))
                .toList();
    }
}
