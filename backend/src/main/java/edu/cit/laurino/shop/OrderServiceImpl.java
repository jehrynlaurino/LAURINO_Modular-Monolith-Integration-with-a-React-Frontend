package edu.cit.laurino.shop;

import edu.cit.laurino.inventory.InventoryItem;
import edu.cit.laurino.inventory.InventoryService;
import edu.cit.laurino.inventory.ReservationResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integrates with InventoryService in-process. There is no HTTP client and no
 * database access to the inventory table from this module. Order state
 * changes are published as domain events for Notification to consume - this
 * class never calls into the notification package directly.
 */
@Service
class OrderServiceImpl implements OrderService {
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    OrderServiceImpl(
            InventoryService inventoryService,
            OrderRepository orderRepository,
            ApplicationEventPublisher eventPublisher) {
        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        List<String> failures = validateAgainstStock(request);

        if (!failures.isEmpty()) {
            String reason = String.join(" ", failures);
            OrderEntity order = new OrderEntity(OrderStatus.REJECTED, reason);
            request.items().forEach(item -> order.addItem(item.productId(), item.quantity()));
            orderRepository.save(order);

            eventPublisher.publishEvent(new OrderRejected(order.getOrderId(), reason));

            List<OrderItemOutcome> outcomes = request.items().stream()
                    .map(item -> new OrderItemOutcome(item.productId(), "REJECTED"))
                    .toList();
            return new OrderResponse(order.getOrderId(), OrderStatus.REJECTED, reason, outcomes, List.of());
        }

        // Everything already passed validation before any reservation was made,
        // so this loop only fails if stock changed concurrently between the
        // validation pass above and this point. Throwing here rolls the whole
        // transaction back, undoing any reservations already made earlier in
        // this same loop - that rollback is what keeps a multi-item order
        // atomic in-process.
        List<InventoryItem> updatedInventory = new ArrayList<>();
        List<OrderItemOutcome> outcomes = new ArrayList<>();
        for (OrderItemRequest item : request.items()) {
            ReservationResult result = inventoryService.reserve(item.productId(), item.quantity());
            if (!result.reserved()) {
                throw new OrderFulfillmentException(
                        "Inventory changed while processing the order: " + result.reason());
            }
            updatedInventory.add(result.inventory());
            outcomes.add(new OrderItemOutcome(item.productId(), "RESERVED"));
        }

        OrderEntity order = new OrderEntity(OrderStatus.CONFIRMED, "Order confirmed.");
        request.items().forEach(item -> order.addItem(item.productId(), item.quantity()));
        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderPlaced(order.getOrderId()));

        return new OrderResponse(
                order.getOrderId(), OrderStatus.CONFIRMED, "Order confirmed.", outcomes, updatedInventory);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderAlreadyCancelledException(orderId);
        }

        // Only a CONFIRMED order actually reserved stock; a REJECTED order
        // never touched inventory, so there is nothing to return for it.
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            for (OrderItemEntity item : order.getItems()) {
                inventoryService.restock(item.getProductId(), item.getQuantity());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);

        List<OrderItemOutcome> outcomes = order.getItems().stream()
                .map(item -> new OrderItemOutcome(item.getProductId(), "CANCELLED"))
                .toList();

        return new OrderResponse(order.getOrderId(), OrderStatus.CANCELLED, "Order cancelled.", outcomes, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummary> listOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderEntity::toSummary)
                .toList();
    }

    private List<String> validateAgainstStock(OrderRequest request) {
        // Aggregate by productId first so two cart lines for the same product
        // are checked against their combined quantity, not each in isolation.
        Map<String, Integer> requestedByProduct = new LinkedHashMap<>();
        for (OrderItemRequest item : request.items()) {
            requestedByProduct.merge(item.productId(), item.quantity(), Integer::sum);
        }

        List<String> failures = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : requestedByProduct.entrySet()) {
            Optional<InventoryItem> inventoryItem = inventoryService.getItem(entry.getKey());
            if (inventoryItem.isEmpty()) {
                failures.add(entry.getKey() + " was not found.");
            } else if (entry.getValue() > inventoryItem.get().stock()) {
                failures.add("Only " + inventoryItem.get().stock() + " unit(s) of "
                        + inventoryItem.get().name() + " remain (requested " + entry.getValue() + ").");
            }
        }
        return failures;
    }
}
