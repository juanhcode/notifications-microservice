package com.develop.notifications_microservice.services;

import com.develop.notifications_microservice.application.use_cases.NotificationService;
import com.develop.notifications_microservice.domain.models.Notification;
import com.develop.notifications_microservice.domain.interfaces.NotificationPersistencePort;
import com.develop.notifications_microservice.infrastructure.messaging.SqsPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationPersistencePort persistencePort;

    @Mock
    private SqsPublisher sqsPublisher;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification(1L, 1L, "Test Description", 2L, true);
    }

    @Test
    void testUpdateNotificationStatus_Success() {
        Long notificationId = notification.getId();
        boolean newStatus = false;

        doNothing().when(persistencePort).updateStatus(notificationId, newStatus);

        notificationService.updateNotificationStatus(notificationId, newStatus);

        verify(persistencePort, times(1)).updateStatus(notificationId, newStatus);
    }

    @Test
    void testUpdateNotificationStatus_NotFound() {
        Long notificationId = 999L;
        boolean newStatus = false;

        doThrow(new IllegalArgumentException("Notification not found with ID: " + notificationId))
                .when(persistencePort).updateStatus(notificationId, newStatus);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.updateNotificationStatus(notificationId, newStatus);
        });

        assertEquals("Notification not found with ID: " + notificationId, exception.getMessage());
    }

    @Test
    void testCreateNotification_Success() {
        // Simulamos que el repositorio guarda la notificación y retorna la misma
        when(persistencePort.save(notification)).thenReturn(notification);

        // Simulamos que SQS publica la notificación
        doNothing().when(sqsPublisher).publishNotification(notification);

        // Llamamos al método del servicio
        Notification createdNotification = notificationService.createNotification(notification);

        // Verificamos que el repositorio y SQS hayan sido llamados correctamente
        verify(persistencePort, times(1)).save(notification);
        verify(sqsPublisher, times(1)).publishNotification(notification);

        // Verificamos que la notificación devuelta es la misma que la creada
        assertNotNull(createdNotification);
        assertEquals(notification, createdNotification);
    }

    @Test
    void testGetNotificationsByUserId() {
        Long userId = 1L;

        // Simulamos que el repositorio devuelve una lista de notificaciones
        List<Notification> notifications = Collections.singletonList(notification);
        when(persistencePort.findByUserId(userId)).thenReturn(notifications);

        // Llamamos al método del servicio
        List<Notification> result = notificationService.getNotificationsByUserId(userId);

        // Verificamos que la lista de notificaciones devuelta no sea vacía
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(notification, result.get(0));

        // Verificamos que el repositorio haya sido llamado correctamente
        verify(persistencePort, times(1)).findByUserId(userId);
    }
}
