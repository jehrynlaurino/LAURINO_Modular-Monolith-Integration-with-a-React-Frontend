package edu.cit.laurino.shop;

import edu.cit.laurino.inventory.InventoryItem;
import java.util.List;

public record OrderResponse(
        Long orderId,
        OrderStatus status,
        String reason,
        List<OrderItemOutcome> items,
        List<InventoryItem> inventory) {
}
