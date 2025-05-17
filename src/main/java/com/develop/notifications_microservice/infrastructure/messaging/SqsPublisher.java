package com.develop.notifications_microservice.infrastructure.messaging;

import com.develop.notifications_microservice.domain.models.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Component
public class SqsPublisher {

    private final SqsClient sqsClient;

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.sqs.queueUrlPublisher}")
    private String queueUrl;

    public SqsPublisher(@Value("${aws.region}") String region,
                        @Value("${aws.accessKey}") String accessKey,
                        @Value("${aws.secretKey}") String secretKey) {
        this.sqsClient = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    public void publishNotification(Notification notification) {
        try {
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(convertNotificationToJson(notification))
                    .messageGroupId("notification-group")
                    .build();

            SendMessageResponse sendMsgResponse = sqsClient.sendMessage(sendMsgRequest);
            System.out.println("📬 Mensaje publicado en SQS con ID: " + sendMsgResponse.messageId());
        } catch (Exception e) {
            System.err.println("❌ Error publicando mensaje en SQS: " + e.getMessage());
        }
    }

    private String convertNotificationToJson(Notification notification) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(notification);
        } catch (Exception e) {
            throw new RuntimeException("Error convirtiendo notificación a JSON", e);
        }
    }
}
