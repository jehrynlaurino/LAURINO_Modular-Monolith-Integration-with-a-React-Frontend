package edu.cit.laurino.inventory;

/**
 * Published after a reservation drops a product's stock below the configured
 * threshold. Notification listens for this; it must never be replaced with a
 * direct call into the notification package.
 */
public record LowStockEvent(String productId, String name, int stock, int threshold) {
}
