package edu.cit.laurino.notification;

import edu.cit.laurino.inventory.LowStockEvent;
import edu.cit.laurino.shop.OrderPlaced;
import edu.cit.laurino.shop.OrderRejected;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Depends only on the event classes published by Order and Inventory - never
 * on OrderService or InventoryService. Listeners run synchronously (no
 * @Async): a notification is written in the same transaction as the order or
 * inventory change that triggered it, so a notification is never recorded for
 * a change that later rolls back. See the README for this trade-off.
 */
@Component
class NotificationEventListener {
    private final NotificationService notificationService;

    NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    void onOrderPlaced(OrderPlaced event) {
        notificationService.record("Order " + event.orderId() + " confirmed");
    }

    @EventListener
    void onOrderRejected(OrderRejected event) {
        notificationService.record("Order " + event.orderId() + " rejected: " + event.reason());
    }

    @EventListener
    void onLowStock(LowStockEvent event) {
        notificationService.record("Reorder needed: " + event.name() + " (" + event.productId()
                + ") has only " + event.stock() + " unit(s) left, below the threshold of "
                + event.threshold() + ".");
    }
}
