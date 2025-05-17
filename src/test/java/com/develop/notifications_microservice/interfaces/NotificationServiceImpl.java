package com.develop.notifications_microservice.interfaces;

import com.develop.notifications_microservice.domain.interfaces.NotificationPersistencePort;
import com.develop.notifications_microservice.domain.interfaces.NotificationServicePort;
import com.develop.notifications_microservice.domain.models.Notification;

import java.util.List;

public class NotificationServiceImpl implements NotificationServicePort {

    private final NotificationPersistencePort persistencePort;

    public NotificationServiceImpl(NotificationPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public Notification createNotification(Notification notification) {
        return persistencePort.save(notification);
    }

    @Override
    public void updateNotificationStatus(Long id, boolean status) {
        persistencePort.updateStatus(id, status);
    }

    @Override
    public List<Notification> getNotificationsByUserId(Long userId) {
        return persistencePort.findByUserId(userId);
    }
}
