package com.develop.notifications_microservice.application.use_cases;

import com.develop.notifications_microservice.domain.models.Notification;
import com.develop.notifications_microservice.domain.interfaces.NotificationPersistencePort;
import com.develop.notifications_microservice.domain.interfaces.NotificationServicePort;
import com.develop.notifications_microservice.infrastructure.messaging.SqsPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService implements NotificationServicePort {

    private final NotificationPersistencePort persistencePort;
    private final SqsPublisher sqsPublisher;

    public NotificationService(NotificationPersistencePort persistencePort, SqsPublisher sqsPublisher) {
        this.persistencePort = persistencePort;
        this.sqsPublisher = sqsPublisher;
    }

    @Override
    public Notification createNotification(Notification notification) {
        // Guardar la notificación en la base de datos
        Notification savedNotification = persistencePort.save(notification);

        // Enviar la notificación a SQS
        sqsPublisher.publishNotification(savedNotification);

        return savedNotification;
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
