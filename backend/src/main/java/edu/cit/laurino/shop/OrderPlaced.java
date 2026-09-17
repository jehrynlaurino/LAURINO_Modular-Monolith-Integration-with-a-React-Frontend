package edu.cit.laurino.shop;

/**
 * Published after an order is confirmed. Notification listens for this;
 * it must never be replaced with a direct call into the notification package.
 */
public record OrderPlaced(Long orderId) {
}
