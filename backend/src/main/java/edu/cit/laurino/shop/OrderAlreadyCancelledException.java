package edu.cit.laurino.shop;

class OrderAlreadyCancelledException extends RuntimeException {
    OrderAlreadyCancelledException(Long orderId) {
        super("Order " + orderId + " is already cancelled.");
    }
}
