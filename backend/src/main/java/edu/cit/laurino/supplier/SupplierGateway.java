package edu.cit.laurino.supplier;

/**
 * Public entry point for the supplier module. Order and Inventory may depend
 * on this interface and the domain types it returns, but never on anything
 * else in this package - no XML classes, no LegacySupply item numbers, pack
 * sizes, or status codes.
 */
public interface SupplierGateway {
    ReorderResult reorder(String productId, int unitsNeeded);
}
