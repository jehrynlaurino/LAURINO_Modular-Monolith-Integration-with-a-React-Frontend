package edu.cit.laurino.shop;

class OrderNotFoundException extends RuntimeException {
    OrderNotFoundException(Long orderId) {
        super("Order " + orderId + " was not found.");
    }
}
