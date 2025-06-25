package com.develop.notifications_microservice.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String description;
    private Long purchaseId;
    private boolean status;
    private LocalDateTime createdOn;
    private String title;

    @PrePersist
    protected void onCreate() {
        createdOn = LocalDateTime.now();
    }
}