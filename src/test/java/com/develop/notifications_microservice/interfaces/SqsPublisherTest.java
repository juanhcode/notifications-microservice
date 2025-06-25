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
}
