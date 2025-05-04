package com.develop.notifications_microservice.domain.interfaces;

import com.develop.notifications_microservice.domain.models.Notification;

import java.util.List;

public interface NotificationPersistencePort {
    Notification save(Notification notification);
    void updateStatus(Long id, boolean status);
    List<Notification> findByUserId(Long userId);
}
