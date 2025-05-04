package com.develop.notifications_microservice.infrastructure.messaging.events;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderEvent {
    private Long orderId;
    private Long userId;
    private Long purchaseId;
    private String description;
}