package com.develop.notifications_microservice.infrastructure.messaging;

import com.develop.notifications_microservice.infrastructure.messaging.events.OrderEvent;
import com.develop.notifications_microservice.domain.interfaces.NotificationServicePort;
import com.develop.notifications_microservice.domain.models.Notification;
import com.develop.notifications_microservice.infrastructure.messaging.events.SnsEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

@Service
public class SqsConsumer {

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final NotificationServicePort notificationServicePort;
    private final ObjectMapper objectMapper;

    public SqsConsumer(@Value("${aws.accessKey}") String accessKey,
                       @Value("${aws.secretKey}") String secretKey,
                       @Value("${aws.sqs.queueUrl}") String queueUrl,
                       NotificationServicePort notificationServicePort,
                       ObjectMapper objectMapper) {
        this.queueUrl = queueUrl;
        this.sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        this.notificationServicePort = notificationServicePort;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void leerMensajes() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .messageAttributeNames("All")
                .build();

        sqsClient.receiveMessage(request).messages().forEach(message -> {
            try {
                System.out.println("📩 Mensaje recibido: " + message.body());
                // Primero deserializamos el SNS Envelope
                SnsEnvelope snsEnvelope = objectMapper.readValue(message.body(), SnsEnvelope.class);

                System.out.println("📩 Mensaje SNS: " + snsEnvelope.getMessage());

                // Ahora deserializamos el contenido real del mensaje SNS (que es tu OrderEvent)
                OrderEvent orderEvent = objectMapper.readValue(snsEnvelope.getMessage(), OrderEvent.class);

                // Procesamos el OrderEvent
                procesarOrderEvent(orderEvent);

                // Eliminamos el mensaje de la cola
                deleteMessage(message.receiptHandle());
            } catch (Exception e) {
                System.err.println("❌ Error procesando mensaje: " + e.getMessage());
            }
        });
    }


    private void deleteMessage(String receiptHandle) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build();
        sqsClient.deleteMessage(deleteRequest);
    }

    private void procesarOrderEvent(OrderEvent orderEvent) {
        // 1. Crear la notificación
        Notification notification = new Notification();
        notification.setUserId(orderEvent.getUserId());
        notification.setPurchaseId(orderEvent.getOrderId());
        notification.setDescription("Estado del pago: " + orderEvent.getPaymentStatus());
        notification.setStatus(true); // O true si ya quieres marcarla como leída

        // 2. Guardar la notificación en la base de datos
        notificationServicePort.createNotification(notification);

        // Log para confirmar la creación de la notificación
        System.out.println("✅ Notificación creada y guardada en la base de datos");
    }

}
