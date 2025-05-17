package com.develop.notifications_microservice.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnsEnvelope {
    @JsonProperty("Type")
    private String type;

    @JsonProperty("MessageId")
    private String messageId;

    @JsonProperty("TopicArn")
    private String topicArn;

    @JsonProperty("Message")
    private String message;  // este es un JSON string

    @JsonProperty("Timestamp")
    private String timestamp;
}
