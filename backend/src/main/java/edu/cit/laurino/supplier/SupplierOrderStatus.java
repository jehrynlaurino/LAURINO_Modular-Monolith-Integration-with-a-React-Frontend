package edu.cit.laurino.supplier;

/**
 * Our own domain status - never LegacySupply's numeric StatusCode.
 */
public enum SupplierOrderStatus {
    PENDING,
    SUBMITTED,
    PICKING,
    SHIPPED,
    DELIVERED,
    FAILED
}
