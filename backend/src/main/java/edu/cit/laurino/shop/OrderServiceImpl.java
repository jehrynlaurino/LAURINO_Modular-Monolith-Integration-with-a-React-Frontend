package edu.cit.laurino.shop;

import edu.cit.laurino.inventory.InventoryService;
import edu.cit.laurino.inventory.ReservationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integrates with InventoryService in-process. There is no HTTP client and no
 * database access to the inventory table from this module.
 */
@Service
class OrderServiceImpl implements OrderService {
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;

    OrderServiceImpl(InventoryService inventoryService, OrderRepository orderRepository) {
        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        ReservationResult reservation = inventoryService.reserve(
                request.productId(), request.quantity());

        OrderStatus status = reservation.reserved()
                ? OrderStatus.CONFIRMED
                : OrderStatus.REJECTED;

        orderRepository.save(new OrderEntity(
                request.productId(),
                request.quantity(),
                status,
                reservation.reason()));

        return new OrderResponse(status, reservation.reason(), reservation.inventory());
    }
}
