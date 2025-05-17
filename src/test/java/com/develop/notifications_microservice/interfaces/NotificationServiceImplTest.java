package com.develop.notifications_microservice.interfaces;

import com.develop.notifications_microservice.domain.interfaces.NotificationPersistencePort;
import com.develop.notifications_microservice.domain.models.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationPersistencePort persistencePort;

    @InjectMocks
    private NotificationServiceImpl service;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setUserId(100L);
        notification.setDescription("Test Notification");
        notification.setPurchaseId(123L);
        notification.setStatus(true);
    }

    @Test
    void testCreateNotificationCallsSave() {
        when(persistencePort.save(notification)).thenReturn(notification);

        Notification result = service.createNotification(notification);

        verify(persistencePort).save(notification);
        assertEquals(notification, result);
    }

    @Test
    void testUpdateNotificationStatusDelegatesToPersistencePort() {
        doNothing().when(persistencePort).updateStatus(1L, false);

        service.updateNotificationStatus(1L, false);

        verify(persistencePort).updateStatus(1L, false);
    }

    @Test
    void testGetNotificationsByUserIdReturnsList() {
        when(persistencePort.findByUserId(100L)).thenReturn(List.of(notification));

        List<Notification> result = service.getNotificationsByUserId(100L);

        verify(persistencePort).findByUserId(100L);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getUserId());
    }
}
