package edu.cit.laurino.notification;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Package-private on purpose, same rule as the other modules' impls.
 */
@Service
class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void record(String message) {
        notificationRepository.save(new NotificationEntity(message));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationView> listNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(NotificationEntity::toView)
                .toList();
    }
}
