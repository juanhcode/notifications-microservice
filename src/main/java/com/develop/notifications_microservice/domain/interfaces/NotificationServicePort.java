package com.develop.notifications_microservice.domain.interfaces;

import com.develop.notifications_microservice.domain.models.Notification;

import java.util.List;

public interface NotificationServicePort {
    Notification createNotification(Notification notification);
    void updateNotificationStatus(Long id, boolean status);
    List<Notification> getNotificationsByUserId(Long userId);
}
