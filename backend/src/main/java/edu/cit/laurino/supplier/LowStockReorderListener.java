package edu.cit.laurino.supplier;

import edu.cit.laurino.inventory.LowStockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to Inventory's LowStockEvent by placing a real purchase order with
 * LegacySupply. Inventory never calls this module directly - it only
 * publishes the event, exactly as it did for Notification in Lab 2. This is
 * the Part C requirement "the auto-reorder rule calls SupplierGateway
 * instead of logging" implemented without adding any coupling to Inventory.
 */
@Component
class LowStockReorderListener {
    private static final Logger log = LoggerFactory.getLogger(LowStockReorderListener.class);

    private final SupplierGateway supplierGateway;

    LowStockReorderListener(SupplierGateway supplierGateway) {
        this.supplierGateway = supplierGateway;
    }

    @EventListener
    void onLowStock(LowStockEvent event) {
        // Top the product back up to twice the low-stock threshold.
        int reorderUpTo = event.threshold() * 2;
        int unitsNeeded = Math.max(reorderUpTo - event.stock(), event.threshold());

        ReorderResult result = supplierGateway.reorder(event.productId(), unitsNeeded);
        if (!result.accepted()) {
            log.warn("Reorder for {} not yet confirmed: {}", event.productId(), result.reason());
        }
    }
}
