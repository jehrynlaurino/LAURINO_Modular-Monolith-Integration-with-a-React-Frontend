package edu.cit.laurino.inventory;

import edu.cit.laurino.supplier.SupplierOrderDelivered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to Supplier's SupplierOrderDelivered event by restocking. Inventory
 * never calls the supplier module directly - it only listens for this event,
 * the same pattern Notification uses for Order's events in Lab 2.
 */
@Component
class SupplierDeliveryRestockListener {
    private static final Logger log = LoggerFactory.getLogger(SupplierDeliveryRestockListener.class);

    private final InventoryService inventoryService;

    SupplierDeliveryRestockListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @EventListener
    void onSupplierOrderDelivered(SupplierOrderDelivered event) {
        inventoryService.restock(event.productId(), event.units());
        log.info("Restocked {} unit(s) of {} from delivered PO {}",
                event.units(), event.productId(), event.poNumber());
    }
}
