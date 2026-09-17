package edu.cit.laurino.shop;

import java.time.Instant;
import java.util.List;

public record OrderSummary(
        Long orderId,
        OrderStatus status,
        String reason,
        Instant createdAt,
        List<OrderItemView> items) {
}
