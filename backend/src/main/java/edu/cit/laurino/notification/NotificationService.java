package edu.cit.laurino.notification;

import java.util.List;

/**
 * Public entry point for the notification module.
 */
public interface NotificationService {
    void record(String message);

    List<NotificationView> listNotifications();
}
