package com.develop.notifications_microservice.infrastructure.messaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.UUID;

@Component
public class SnsPublisher {

    private final SnsClient snsClient;

    @Value("${aws.sns.topicArn}")
    private String topicArn;

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    public SnsPublisher(@Value("${aws.region}") String region, @Value("${aws.accessKey}") String accessKey,
                        @Value("${aws.secretKey}") String secretKey) {
        this.snsClient = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    public void publishNotification(String message) {
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .messageGroupId("notification-group")  // Solo si la cola es FIFO
                .messageDeduplicationId(UUID.randomUUID().toString()) // Mejor para evitar duplicados
                .build();

        snsClient.publish(request);
        System.out.println("📬 Mensaje publicado en SNS");
    }

}
