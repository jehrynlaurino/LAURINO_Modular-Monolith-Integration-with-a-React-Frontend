package edu.cit.laurino.shop;

import java.util.List;

/**
 * Public entry point for the order module.
 */
public interface OrderService {
    OrderResponse placeOrder(OrderRequest request);

    OrderResponse cancelOrder(Long orderId);

    List<OrderSummary> listOrders();
}
