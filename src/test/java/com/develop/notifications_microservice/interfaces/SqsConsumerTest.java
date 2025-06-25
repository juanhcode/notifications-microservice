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

    private SqsConsumer sqsConsumer;

    @BeforeEach
    void setUp() {
        sqsConsumer = new SqsConsumer(
                "dummyAccessKey",
                "dummySecretKey",
                "dummyQueueUrl",
                notificationServicePort,
                new ObjectMapper()
        );
    }

    @Test
    void procesarOrderEvent_shouldCreateNotification_whenDeliveryProcessingAndPaid() {
        OrderEvent order = buildOrder(1L, 2L, "Processing", "Paid");

        sqsConsumer.procesarOrderEvent(order);

        verifyAndAssertNotification("Pedido en proceso",
                "Tu pedido #1 está siendo preparado.", order, false);
    }

    @Test
    void procesarOrderEvent_shouldCreateNotification_whenDeliveryShippedAndFailed() {
        OrderEvent order = buildOrder(3L, 4L, "Shipped", "Failed");

        sqsConsumer.procesarOrderEvent(order);

        verifyAndAssertNotification("Pedido enviado",
                "Tu pedido #3 ha sido enviado. | Estado de pago: Pago fallido", order, false);
    }

    @Test
    void procesarOrderEvent_shouldCreateNotification_whenDeliveryDeliveredAndOnHold() {
        OrderEvent order = buildOrder(5L, 6L, "Delivered", "On Hold");

        sqsConsumer.procesarOrderEvent(order);

        verifyAndAssertNotification("Pedido entregado",
                "¡Tu pedido #5 ha sido entregado! | Estado de pago: Pago en espera", order, false);
    }

    @Test
    void procesarOrderEvent_shouldCreateNotification_whenDeliveryIsUnknown() {
        OrderEvent order = buildOrder(7L, 8L, "UnknownStatus", "Cancelled");

        sqsConsumer.procesarOrderEvent(order);

        verifyAndAssertNotification("Actualización de pedido",
                "Tu pedido #7 tiene un nuevo estado: UnknownStatus | Estado de pago: Pago cancelado", order, false);
    }

    @Test
    void procesarOrderEvent_shouldCreateNotification_whenDeliveryDeliveredAndPaymentPaid() {
        OrderEvent order = buildOrder(9L, 10L, "Delivered", "Paid");

        sqsConsumer.procesarOrderEvent(order);

        verifyAndAssertNotification("Pedido entregado",
                "¡Tu pedido #9 ha sido entregado!", order, false);
    }

    private OrderEvent buildOrder(Long orderId, Long userId, String deliveryStatus, String paymentStatus) {
        OrderEvent event = new OrderEvent();
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setStatusDeliveryName(deliveryStatus);
        event.setPaymentStatusName(paymentStatus);
        return event;
    }

    private void verifyAndAssertNotification(String expectedTitle, String expectedDescStart, OrderEvent order, boolean expectedStatus) {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationServicePort, times(1)).createNotification(captor.capture());

        Notification n = captor.getValue();

        assertEquals(order.getUserId(), n.getUserId());
        assertEquals(order.getOrderId(), n.getPurchaseId());
        assertEquals(expectedTitle, n.getTitle());
        assertTrue(n.getDescription().startsWith(expectedDescStart) || n.getDescription().contains(expectedDescStart));
        assertEquals(expectedStatus, n.isStatus());
    }
}
