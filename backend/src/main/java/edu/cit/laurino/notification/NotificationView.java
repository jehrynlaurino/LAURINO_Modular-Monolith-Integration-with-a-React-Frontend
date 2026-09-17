package edu.cit.laurino.notification;

import java.time.Instant;

/**
 * Public data owned by the notification module.
 */
public record NotificationView(Long notificationId, String message, Instant createdAt) {
}
