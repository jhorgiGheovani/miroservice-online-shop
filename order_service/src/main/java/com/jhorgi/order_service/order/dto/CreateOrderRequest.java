package com.jhorgi.order_service.order.dto;

import java.util.List;

public class CreateOrderRequest {

    private List<OrderItemRequest> items;

    public List<OrderItemRequest> getItems() { return items; }
}
