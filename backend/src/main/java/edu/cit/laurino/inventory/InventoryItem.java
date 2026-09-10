package edu.cit.laurino.inventory;

/**
 * Public data owned by the inventory module and safe for the order module to read.
 */
public record InventoryItem(String productId, String name, int stock) {
}
