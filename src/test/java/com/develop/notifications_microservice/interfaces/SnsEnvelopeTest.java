package com.develop.notifications_microservice.interfaces;

import com.develop.notifications_microservice.infrastructure.messaging.events.SnsEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnsEnvelopeTest {

    @Test
    void testSettersAndGetters() {
        SnsEnvelope envelope = new SnsEnvelope();

        envelope.setType("Notification");
        envelope.setMessageId("abc-123");
        envelope.setTopicArn("arn:aws:sns:us-east-1:123456789012:MyTopic");
        envelope.setMessage("{\"orderId\":123,\"userId\":456}");
        envelope.setTimestamp("2024-06-25T12:00:00Z");

        assertEquals("Notification", envelope.getType());
        assertEquals("abc-123", envelope.getMessageId());
        assertEquals("arn:aws:sns:us-east-1:123456789012:MyTopic", envelope.getTopicArn());
        assertEquals("{\"orderId\":123,\"userId\":456}", envelope.getMessage());
        assertEquals("2024-06-25T12:00:00Z", envelope.getTimestamp());
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = """
                {
                    "Type": "Notification",
                    "MessageId": "abc-123",
                    "TopicArn": "arn:aws:sns:us-east-1:123456789012:MyTopic",
                    "Message": "{\\"orderId\\":123,\\"userId\\":456}",
                    "Timestamp": "2024-06-25T12:00:00Z"
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper();
        SnsEnvelope envelope = objectMapper.readValue(json, SnsEnvelope.class);

        assertEquals("Notification", envelope.getType());
        assertEquals("abc-123", envelope.getMessageId());
        assertEquals("arn:aws:sns:us-east-1:123456789012:MyTopic", envelope.getTopicArn());
        assertEquals("{\"orderId\":123,\"userId\":456}", envelope.getMessage());
        assertEquals("2024-06-25T12:00:00Z", envelope.getTimestamp());
    }
}
