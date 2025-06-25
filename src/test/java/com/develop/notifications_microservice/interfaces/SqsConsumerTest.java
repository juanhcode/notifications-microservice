package com.develop.notifications_microservice.interfaces;

import com.develop.notifications_microservice.domain.interfaces.NotificationServicePort;
import com.develop.notifications_microservice.domain.models.Notification;
import com.develop.notifications_microservice.infrastructure.messaging.SqsConsumer;
import com.develop.notifications_microservice.infrastructure.messaging.events.OrderEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsConsumerTest {

    @Mock
    private NotificationServicePort notificationServicePort;

    @InjectMocks
    private SqsConsumer sqsConsumer = new SqsConsumer(
            "dummyAccessKey",
            "dummySecretKey",
            "dummyQueueUrl",
            mock(NotificationServicePort.class),
            new ObjectMapper()
    );

    @BeforeEach
    void setUp() {
        // Reemplazar el mock real dentro del InjectMocks si es necesario
        sqsConsumer = new SqsConsumer(
                "dummyAccessKey",
                "dummySecretKey",
                "dummyQueueUrl",
                notificationServicePort,
                new ObjectMapper()
        );
    }

    @Test
    void procesarOrderEvent_shouldCreateNotificationCorrectly_whenDeliveryIsDeliveredAndPaymentPending() {
        // Arrange
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(123L);
        orderEvent.setUserId(5L);
        orderEvent.setStatusDeliveryName("Delivered");
        orderEvent.setPaymentStatusName("Pending");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // Act
        sqsConsumer.procesarOrderEvent(orderEvent);

        // Assert
        verify(notificationServicePort, times(1)).createNotification(captor.capture());

        Notification notif = captor.getValue();

        assertEquals("Pedido entregado", notif.getTitle());
        assertTrue(notif.getDescription().contains("¡Tu pedido #123 ha sido entregado!"));
        assertTrue(notif.getDescription().contains("Estado de pago: Pendiente de pago"));
        assertEquals(5L, notif.getUserId());
        assertEquals(123L, notif.getPurchaseId());
        assertFalse(notif.isStatus());
    }
}
