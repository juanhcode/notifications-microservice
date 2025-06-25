package com.develop.notifications_microservice.infrastructure.messaging.events;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class OrderEvent {
    private Long orderId;
    private Long userId;
    private String paymentStatusName;
    private String statusDeliveryName;
    private int[] productIds;
}