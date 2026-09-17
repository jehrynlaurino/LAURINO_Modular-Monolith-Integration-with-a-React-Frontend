package edu.cit.laurino.shop;

/**
 * Thrown if stock changes between the pre-reservation validation pass and the
 * actual reservation loop (e.g. a concurrent order). Being a RuntimeException
 * thrown inside the @Transactional placeOrder method, it forces a rollback of
 * any reservations already made earlier in that same loop.
 */
class OrderFulfillmentException extends RuntimeException {
    OrderFulfillmentException(String message) {
        super(message);
    }
}
