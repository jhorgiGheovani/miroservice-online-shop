package com.jhorgi.order_service.order.controller;

import com.jhorgi.order_service.order.dto.CreateOrderRequest;
import com.jhorgi.order_service.order.entity.Order;
import com.jhorgi.order_service.order.service.OrderService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request,
                                             HttpServletRequest httpRequest) {
        Claims claims = (Claims) httpRequest.getAttribute("jwtClaims");
        String customerId = claims.getSubject();
        Order order = orderService.createOrder(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
