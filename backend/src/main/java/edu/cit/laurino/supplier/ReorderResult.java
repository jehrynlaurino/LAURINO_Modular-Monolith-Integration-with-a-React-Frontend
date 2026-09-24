package edu.cit.laurino.supplier;

/**
 * Our own result type - never LegacySupply's XML acknowledgement or its
 * status codes.
 */
public record ReorderResult(boolean accepted, String reason, Long supplierOrderId, String poNumber) {
}
