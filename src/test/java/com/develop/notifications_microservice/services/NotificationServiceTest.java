package com.develop.notifications_microservice.services;
import com.develop.notifications_microservice.application.use_cases.NotificationService;
import com.develop.notifications_microservice.domain.interfaces.NotificationPersistencePort;
import com.develop.notifications_microservice.domain.models.Notification;
import com.develop.notifications_microservice.infrastructure.messaging.SqsPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        notification = new Notification(
                1L,
                1L,
                "Test Description",
                2L,
                true,
                LocalDateTime.now(),
                "Título de prueba"
        );
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
        when(persistencePort.save(notification)).thenReturn(notification);
        doNothing().when(sqsPublisher).publishNotification(notification);

        Notification createdNotification = notificationService.createNotification(notification);

        verify(persistencePort, times(1)).save(notification);
        verify(sqsPublisher, times(1)).publishNotification(notification);

        assertNotNull(createdNotification);
        assertEquals(notification, createdNotification);
    }

    @Test
    void testGetNotificationsByUserId() {
        Long userId = 1L;
        List<Notification> notifications = Collections.singletonList(notification);
        when(persistencePort.findByUserId(userId)).thenReturn(notifications);

        List<Notification> result = notificationService.getNotificationsByUserId(userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(notification, result.get(0));
        verify(persistencePort, times(1)).findByUserId(userId);
    }
}
