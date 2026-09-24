package edu.cit.laurino.supplier;

/**
 * Published when a purchase order's status transitions to DELIVERED.
 * Inventory listens for this to restock; it must never be replaced with a
 * direct call into the inventory package.
 */
public record SupplierOrderDelivered(String productId, int units, String poNumber) {
}
