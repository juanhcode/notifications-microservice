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

    @Scheduled(fixedDelay = 1000)
    public void leerMensajes() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .messageAttributeNames("All")
                .build();

        sqsClient.receiveMessage(request).messages().forEach(message -> {
            try {
                System.out.println("📩 Mensaje recibido:");
                System.out.println("🟡 ID: " + message.messageId());
                System.out.println("🟡 Cuerpo: " + message.body());
                System.out.println("🟡 Atributos: " + message.attributes());
                System.out.println("🟡 Atributos personalizados: " + message.messageAttributes());

                System.out.println("📩 Mensaje recibido: " + message.body());
                // Primero deserializamos el SNS Envelope
                SnsEnvelope snsEnvelope = objectMapper.readValue(message.body(), SnsEnvelope.class);

                System.out.println("📩 Mensaje SNS: " + snsEnvelope.getMessage());

                // Ahora deserializamos el contenido real del mensaje SNS (que es tu OrderEvent)
                OrderEvent orderEvent = objectMapper.readValue(snsEnvelope.getMessage(), OrderEvent.class);

                System.out.println("🟢 OrderEvent recibido:");
                System.out.println("    🧾 Order ID: " + orderEvent.getOrderId());
                System.out.println("    👤 User ID: " + orderEvent.getUserId());
                System.out.println("    💳 Payment Status: " + orderEvent.getPaymentStatusName());

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
        // Crear la notificación con título y descripción según los estados
        Notification notification = new Notification();
        notification.setUserId(orderEvent.getUserId());
        notification.setPurchaseId(orderEvent.getOrderId());
        notification.setStatus(false); // Por defecto no leída

        // Configurar título y descripción según el estado de delivery
        String deliveryStatus = orderEvent.getStatusDeliveryName();
        String paymentStatus = orderEvent.getPaymentStatusName();

        switch(deliveryStatus) {
            case "Processing":
                notification.setTitle("Pedido en proceso");
                notification.setDescription("Tu pedido #" + orderEvent.getOrderId() + " está siendo preparado.");
                break;
            case "Shipped":
                notification.setTitle("Pedido enviado");
                notification.setDescription("Tu pedido #" + orderEvent.getOrderId() + " ha sido enviado.");
                break;
            case "Delivered":
                notification.setTitle("Pedido entregado");
                notification.setDescription("¡Tu pedido #" + orderEvent.getOrderId() + " ha sido entregado!");
                break;
            default:
                notification.setTitle("Actualización de pedido");
                notification.setDescription("Tu pedido #" + orderEvent.getOrderId() + " tiene un nuevo estado: " + deliveryStatus);
        }

        // Si el estado de pago es importante, podemos agregarlo a la descripción
        if (!"Paid".equals(paymentStatus)) {
            notification.setDescription(notification.getDescription() +
                    " | Estado de pago: " + getPaymentStatusDescription(paymentStatus));
        }

        // Guardar la notificación en la base de datos
        notificationServicePort.createNotification(notification);

        System.out.println("✅ Notificación creada y guardada en la base de datos");
    }

    private String getPaymentStatusDescription(String paymentStatus) {
        switch(paymentStatus) {
            case "Pending":
                return "Pendiente de pago";
            case "Failed":
                return "Pago fallido";
            case "Refunded":
                return "Reembolsado";
            case "Partially Refunded":
                return "Reembolsado parcialmente";
            case "Cancelled":
                return "Pago cancelado";
            case "On Hold":
                return "Pago en espera";
            case "Processing":
                return "Pago en proceso";
            default:
                return paymentStatus;
        }
    }

}
