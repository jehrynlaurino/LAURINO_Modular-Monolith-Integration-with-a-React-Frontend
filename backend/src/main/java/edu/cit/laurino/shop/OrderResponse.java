package edu.cit.laurino.shop;

import edu.cit.laurino.inventory.InventoryItem;

public record OrderResponse(
        OrderStatus status,
        String reason,
        InventoryItem inventory) {
}
