package com.develop.notifications_microservice.domain.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode  // ✅ Agrega comparación por contenido (necesario para assertEquals)
@ToString           // ✅ Opcional: útil si usas assertTrue(toString().contains(...))
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