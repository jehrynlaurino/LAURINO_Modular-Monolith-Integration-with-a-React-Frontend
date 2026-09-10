package edu.cit.laurino.shop;

/**
 * Public entry point for the order module.
 */
public interface OrderService {
    OrderResponse placeOrder(OrderRequest request);
}
