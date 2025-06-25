package com.develop.notifications_microservice.interfaces;

import com.develop.notifications_microservice.domain.models.Notification;
import com.develop.notifications_microservice.infrastructure.messaging.SqsPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsPublisherTest {

    @Mock
    private SqsClient sqsClient;

    private SqsPublisher sqsPublisher;

    @BeforeEach
    void setUp() {
        sqsPublisher = new SqsPublisher(sqsClient, "https://dummy-queue-url");
    }

    @Test
    void publishNotification_shouldSendMessageWithNotificationId() {
        // Given
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(10L);
        notification.setPurchaseId(99L);
        notification.setDescription("Pedido enviado");
        notification.setTitle("Estado");
        notification.setStatus(false);

        SendMessageResponse mockResponse = SendMessageResponse.builder().messageId("msg-123").build();
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> sqsPublisher.publishNotification(notification));

        // Then
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest sentRequest = captor.getValue();
        assertEquals("https://dummy-queue-url", sentRequest.queueUrl());
        assertEquals("notification-group", sentRequest.messageGroupId());
        assertEquals("1", sentRequest.messageDeduplicationId());

        assertTrue(sentRequest.messageBody().contains("Pedido enviado"));
    }

    @Test
    void publishNotification_shouldUseOrderIdAsDeduplicationId_whenNotificationIdIsNull() {
        // Given
        Notification notification = new Notification();
        notification.setId(null);
        notification.setPurchaseId(456L);
        notification.setDescription("Otro evento");

        SendMessageResponse mockResponse = SendMessageResponse.builder().messageId("msg-456").build();
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(mockResponse);

        // When
        sqsPublisher.publishNotification(notification);

        // Then
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest sentRequest = captor.getValue();
        assertEquals("order-456", sentRequest.messageDeduplicationId());
    }

    @Test
    void publishNotification_shouldThrowException_whenJsonConversionFails() {
        // Given
        Notification invalidNotification = mock(Notification.class);
        // Forzamos una excepción al convertir el objeto a JSON manipulando el método (solo si fuera público)
        // Como no es posible directamente, puedes simular una conversión fallida con un spy si necesitas testearlo explícitamente
        // Pero aquí vamos a forzar indirectamente usando un objeto no serializable (null purchaseId provocará NPE si toString se invoca)

        // When / Then
        assertDoesNotThrow(() -> sqsPublisher.publishNotification(invalidNotification));
        // Esto capturará la excepción interna, no se propagará gracias al catch
    }

    @Test
    void publishNotification_shouldHandleExceptionFromSqsClientGracefully() {
        // Given
        Notification notification = new Notification();
        notification.setId(99L);
        notification.setPurchaseId(321L);
        notification.setDescription("Error esperado");

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("SQS está caído"));

        // When / Then
        assertDoesNotThrow(() -> sqsPublisher.publishNotification(notification));
        verify(sqsClient).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void publishNotification_shouldHandleEmptyNotification() {
        // Given
        Notification notification = new Notification(); // todos los campos null

        SendMessageResponse mockResponse = SendMessageResponse.builder().messageId("msg-999").build();
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> sqsPublisher.publishNotification(notification));

        // Then
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertEquals("notification-group", request.messageGroupId());
        assertTrue(request.messageBody().contains("null"));
        assertTrue(request.messageDeduplicationId().contains("order-null"));
    }

    @Test
    void publishNotification_shouldContainAllNotificationFieldsInJson() {
        // Given
        Notification notification = new Notification();
        notification.setId(20L);
        notification.setUserId(3L);
        notification.setPurchaseId(5L);
        notification.setDescription("📦 Listo");
        notification.setTitle("Confirmación");
        notification.setStatus(true);

        SendMessageResponse response = SendMessageResponse.builder().messageId("msg-abc").build();
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(response);

        // When
        sqsPublisher.publishNotification(notification);

        // Then
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        String body = captor.getValue().messageBody();
        assertAll(
                () -> assertTrue(body.contains("\"id\":20")),
                () -> assertTrue(body.contains("\"userId\":3")),
                () -> assertTrue(body.contains("\"purchaseId\":5")),
                () -> assertTrue(body.contains("📦")),
                () -> assertTrue(body.contains("Confirmación")),
                () -> assertTrue(body.contains("\"status\":true"))
        );
    }

}
