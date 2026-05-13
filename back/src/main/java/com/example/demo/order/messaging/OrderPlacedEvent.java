package com.example.demo.order.messaging;

public record OrderPlacedEvent(
        Long orderId,
        Long businessId,
        Long userId
) {}
